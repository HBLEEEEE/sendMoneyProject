package assignment.MoinTest.transfer.service;


import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.ListResponse;
import assignment.MoinTest.Response.QuoteResponse;
import assignment.MoinTest.common.exception.CustomException;
import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import assignment.MoinTest.security.UserDetailsImpl;
import assignment.MoinTest.transfer.dto.Forex;
import assignment.MoinTest.transfer.dto.HistoryResponseDto;
import assignment.MoinTest.transfer.dto.QuoteRequestDto;
import assignment.MoinTest.transfer.dto.RequestDto;
import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.transfer.entity.Request;
import assignment.MoinTest.transfer.repository.QuoteRepository;
import assignment.MoinTest.transfer.repository.RequestRepository;
import assignment.MoinTest.user.entity.User;
import assignment.MoinTest.user.entity.UserIdTypeEnum;
import assignment.MoinTest.user.repository.UserRepository;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static Comparator<LocalDateTime> comparator = LocalDateTime::compareTo;


    @Transactional
    public ResponseEntity<QuoteResponse> orderQuote(QuoteRequestDto quoteRequestDto, UserDetailsImpl userDetails){
        double exchangeRate;
        double exchangeUnit;
        double usdRate;
        double usdUnit;
        double fee;
        double totalFee;
        double pow;
        double amount = quoteRequestDto.getAmount();
        String targetCurrency = quoteRequestDto.getTargetCurrency();

        User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
        if (user == null){
            return QuoteResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        //0원 미만의 요청 예외 처리
        if(quoteRequestDto.getAmount()<0){
            return QuoteResponse.toResponseEntity(ErrorCode.NEGATIVE_NUMBER);
        }

        //JPY 또는 USD만 취급
        if(targetCurrency.equals("JPY") || targetCurrency.equals("USD")){
            double[][] baseAndUnit = getBaseAndUnit(targetCurrency);
            exchangeRate = baseAndUnit[0][0];
            exchangeUnit = baseAndUnit[0][1];
            usdRate = baseAndUnit[1][0];
            usdUnit = baseAndUnit[1][1];
        }else{
            //잘못된 targetCurrency 예외 처리
            return QuoteResponse.toResponseEntity(ErrorCode.WRONG_TARGETCURRENCY);
        }

        // 국가 및 금액별 수수료 체결
        if(targetCurrency.equals("USD")){
            //100만원이상인지 초과인지 조건을 잘 모르겠다.
            if (amount>1000000){
                fee = 3000;
                totalFee = amount * 0.001 + fee;
            }else{
                fee = 1000;
                totalFee = amount * 0.002 + fee;
            }
            pow = Currency.getInstance("USD").getDefaultFractionDigits();
        } else if (targetCurrency.equals("JPY")) {
            fee = 3000;
            totalFee = amount * 0.005 + fee;
            pow = Currency.getInstance("JPY").getDefaultFractionDigits();
        }else{
            return QuoteResponse.toResponseEntity(ErrorCode.WRONG_TARGETCURRENCY);
        }

        double usdAmount = Math.round(amount * usdUnit / usdRate *100)/100;
        double targetAmount = (amount-totalFee) * exchangeUnit / exchangeRate;

        double round = Math.pow(10.0, pow);
        targetAmount = Math.round(targetAmount*round) / round;

        //보낼 금액 음수인 경우 예외 처리
        if(targetAmount<0){
            return QuoteResponse.toResponseEntity(ErrorCode.NEGATIVE_NUMBER);
        }

        Quote quote = new Quote();
        quote.setSourceAmount(quoteRequestDto.getAmount());
        quote.setFee(fee);
        quote.setUseExchangeRate(usdRate);
        quote.setUsdAmount(usdAmount);
        quote.setTargetCurrency(targetCurrency);
        quote.setExchangeRate(exchangeRate);
        quote.setTargetAmount(targetAmount);
        quote.setRequestedAt(LocalDateTime.now());
        quote.setUser(user);

        quoteRepository.save(quote);

        return QuoteResponse.toResponseEntity(SuccessCode.SAVE_QUOTE, quote);
    }

    public double[][] getBaseAndUnit(String targetCurrency){
        String target = "FRX.KRW" + targetCurrency + ",FRX.KRWUSD";

        URI uri = UriComponentsBuilder
                .fromUriString("https://quotation-api-cdn.dunamu.com:443")
                .path("/v1/forex/recent")
                .queryParam("codes",target)
                .encode()
                .build()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Forex[]> result = restTemplate.getForEntity(uri, Forex[].class);
        Forex[] forexArray = result.getBody();

        double firstBase = forexArray[0].getBasePrice();
        double firstUnit = forexArray[0].getCurrencyUnit();
        double usdBase = forexArray[1].getBasePrice();
        double usdUnit = forexArray[1].getCurrencyUnit();

        double[][] arr = new double[][]{{firstBase, firstUnit}, {usdBase, usdUnit}};

        return arr;
    }

    @Transactional
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public ResponseEntity<BaseResponse> requestQuote(RequestDto requestDto, UserDetailsImpl userDetails) {

        User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
        if (user==null){
            return BaseResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        Quote quote = quoteRepository.findById(requestDto.getQuoteId()).orElse(null);
        if (quote==null){
            return BaseResponse.toResponseEntity(ErrorCode.QUOTE_NOT_FOUND);
        }

        if(quote.getRequest() != null){
            return BaseResponse.toResponseEntity(ErrorCode.QUOTE_ALREADY_USED);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime quoteCreate = quote.getRequestedAt();
        Duration duration = Duration.between(now, quoteCreate);

        if(Math.abs(duration.toMinutes()) <= 10){
            LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
            LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
            List<Request> requests = requestRepository.findByUserAndRequestTimeBetween(user, startOfToday, endOfToday);
            double total = 0;

            for (int i = 0; i < requests.size(); i++) {
                total += requests.get(i).getQuote().getUsdAmount();
            }

            total+=quote.getUsdAmount();

            if (user.getIdType().equals(UserIdTypeEnum.REG_NO) && total >= 1000){
                return BaseResponse.toResponseEntity(ErrorCode.LIMIT_EXCESS);
            } else if (user.getIdType().equals(UserIdTypeEnum.BUSINESS_NO) && total >= 5000) {
                return BaseResponse.toResponseEntity(ErrorCode.LIMIT_EXCESS);
            }

            Request request = new Request();
            request.setQuote(quote);
            request.setUser(user);
            request.setRequestTime(now);
            requestRepository.save(request);
            quote.setRequest(request);

            return BaseResponse.toResponseEntity(SuccessCode.REQUEST_QUOTE);
        }else {
            return BaseResponse.toResponseEntity(ErrorCode.QUOTE_EXPIRED);
        }
    }


    @Transactional
    public ResponseEntity<ListResponse> getHistories(UserDetailsImpl userDetails) {

        User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
        if (user==null){
            return ListResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        List<Quote> quotes = quoteRepository.findAllByUser(user);

        int todayCnt = 0;
        double todayTotalUsd = 0;
        List<HistoryResponseDto> histories = new ArrayList<>();
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간

        for (int i = quotes.size()-1; i >= 0; i--) {

            Quote quote = quotes.get(i);

            if (quote.getRequest() == null){
                continue;
            }

            LocalDateTime requestTime = quote.getRequestedAt();
            if (comparator.compare(requestTime, startOfToday) >=0 && comparator.compare(requestTime,endOfToday) <=0 ){
                todayCnt++;
                todayTotalUsd += quote.getUsdAmount();
            }

            HistoryResponseDto hRD = new HistoryResponseDto();
            hRD.setSourceAmount(quote.getSourceAmount());
            hRD.setFee(quote.getFee());
            hRD.setUsdExchangeRate(quote.getUseExchangeRate());
            hRD.setUsdAmount(quote.getUsdAmount());
            hRD.setTargetCurrency(quote.getTargetCurrency());
            hRD.setExchangeRate(quote.getExchangeRate());
            hRD.setTargetAmount(quote.getTargetAmount());
            hRD.setRequestedDate(quote.getRequestedAt().format(formatter));

            histories.add(hRD);

        }

        return ListResponse.toResponseEntity(SuccessCode.GET_HISTORIES, user, todayCnt, todayTotalUsd, histories);

    }
}
