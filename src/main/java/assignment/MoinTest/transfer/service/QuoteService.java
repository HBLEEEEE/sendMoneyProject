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
import java.time.temporal.TemporalUnit;
import java.util.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDateTime;

import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

@Service
@RequiredArgsConstructor
public class QuoteService {
    // TODO: 1. 별거아니지만 일반적인 순서들이 잇음  https://www.oracle.com/java/technologies/javase/codeconventions-fileorganization.html
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static Comparator<LocalDateTime> comparator = LocalDateTime::compareTo;

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Transactional
    public ResponseEntity<QuoteResponse> orderQuote(QuoteRequestDto quoteRequestDto, UserDetailsImpl userDetails){

        // TODO: 1. 자바는 보통 호출 직전에 선언해둠 클래스나 메서드 최상단에 미리 선언하는건 약간 C스타일인듯
        double exchangeRate;
        double exchangeUnit;
        double usdRate;
        double usdUnit;
        double fee;
        double totalFee;
        double pow;
        double amount = quoteRequestDto.getAmount();
        String targetCurrency = quoteRequestDto.getTargetCurrency();

        // TODO: 2. 이런식으로 orElse(null), user.isEmptry, .ifPresent 같은 방식으로 사용하는건 null check 방식과 크게 다르지 않아서 차라리 optional을 안써도 좋을듯
        User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
        if (user == null){
            return QuoteResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        //0원 미만의 요청 예외 처리
        if(quoteRequestDto.getAmount()<0){
            return QuoteResponse.toResponseEntity(ErrorCode.NEGATIVE_NUMBER);
        }

        // TODO: 3. targetCurrency enum으로 빼도 좋을듯
        // if return은 빠르게
        //JPY 또는 USD만 취급
        if(!targetCurrency.equals("JPY") && !targetCurrency.equals("USD")){
            //잘못된 targetCurrency 예외 처리
            return QuoteResponse.toResponseEntity(ErrorCode.WRONG_TARGETCURRENCY);
        }

        // 이차원 배열보단 객체를 만드는게 좋을듯
        double[][] baseAndUnit = getBaseAndUnit(targetCurrency);
        exchangeRate = baseAndUnit[0][0];
        exchangeUnit = baseAndUnit[0][1];
        usdRate = baseAndUnit[1][0];
        usdUnit = baseAndUnit[1][1];


        // usd, jpy등 통화별로 클래스들 만들면 좋을듯
        // currencyServiceFactory.find("USD").calculateTotalFee(amount)
        //
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
            // 이건 위에서 체크햇자나
            return QuoteResponse.toResponseEntity(ErrorCode.WRONG_TARGETCURRENCY);
        }

        // 이런 계산들도 의미가 들어나게 한줄이어도 메소드 추출하거나 상수화 가능한건 상수화 하고 숫자 그대로가 의미인거는 상수화 안해도됨
        double usdAmount = Math.round(amount * usdUnit / usdRate *100)/100;
        double targetAmount = (amount-totalFee) * exchangeUnit / exchangeRate;

        double round = Math.pow(10.0, pow);
        // 이런건 서비스 코드나 제출하는 곳에 있으면 안댈듯 log4j2, logback 나중에 공부하고 적용해보샘
        System.out.println("round = " + round);
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

        // 체크는 빨리 끝내고 리턴해야 아래 코드가 뎁스가 안생겨서 복잡해지지 않음
        if(Math.abs(duration.toMinutes()) > 10) {
            return BaseResponse.toResponseEntity(ErrorCode.QUOTE_EXPIRED);
        }



        // 토탈 체크도 메서드 추출하면 좋을듯
            LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
            LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
            List<Request> requests = requestRepository.findByRequestTimeBetween(startOfToday, endOfToday);
            double total = 0;

            // 스트림 써야 간지남
            // Double total = requests.stream()
            //     .map(Request::getQuote)
            //     .map(Quote::getUsdAmount)
            //     .reduce(0D, Double::sum);
            // total += +=quote.getUsdAmount();

            for (int i = 0; i < requests.size(); i++) {
                total += requests.get(i).getQuote().getUsdAmount();
            }

            total+=quote.getUsdAmount();

            // 이넘은 == 비교 가능
            if (user.getIdType() == UserIdTypeEnum.REG_NO && total >= 1000){
                return BaseResponse.toResponseEntity(ErrorCode.LIMIT_EXCESS);
            } else if (user.getIdType().equals(UserIdTypeEnum.BUSINESS_NO) && total >= 5000) {
                return BaseResponse.toResponseEntity(ErrorCode.LIMIT_EXCESS);
            }

            Request request = new Request();
            request.setQuote(quote);
            request.setUser(user);
            request.setRequestTime(now);
            requestRepository.save(request);

            // 이건 머임??
            quote.setRequest(request);

            return BaseResponse.toResponseEntity(SuccessCode.REQUEST_QUOTE);
    }


    //TODO 트랜잭션 사용의도 궁금 조회 밖에 없어보이는데 베타락 공유락한거면 isolation 레벨도 체크해야할듯
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

            // TODO 단순 날자비교인데 커페어레이터 쓸필요 없음 보통 커스텀 정렬할때 이용
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

        // TODO 기능별로 적당히 클래스 나 메서드 추출 하면 좋을듯 몇번 더 돌아도 코드 보기 좋게, 이건 꼭 이렇게 안해도 될듯 참고
        //
        // List<HistoryResponseDto> histories = quotes.stream()
        //     .filter(it -> it.getRequest() != null)
        // // it는 내가 걍 쓴거고 history이런식으로 명확한 변수명해주는게 보통인듯
        //     .map(it -> {
        //         HistoryResponseDto historyResponseDto = new HistoryResponseDto();
        //         historyResponseDto.setSourceAmount(it.getSourceAmount());
        //         historyResponseDto.setFee(it.getFee());
        //         historyResponseDto.setUsdExchangeRate(it.getUseExchangeRate());
        //         historyResponseDto.setUsdAmount(it.getUsdAmount());
        //         historyResponseDto.setTargetCurrency(it.getTargetCurrency());
        //         historyResponseDto.setExchangeRate(it.getExchangeRate());
        //         historyResponseDto.setTargetAmount(it.getTargetAmount());
        //         historyResponseDto.setRequestedDate(it.getRequestedAt().format(formatter));
        //
        //         return historyResponseDto;
        //     })
        //     .toList();

        // LocalDate nowDate = LocalDateTime.now().toLocalDate();
        // List<Quote> todayQuoteList = quotes.stream()
        //     .filter(quote -> nowDate.isEqual(quote.getRequestedAt().toLocalDate()))
        //     .toList();

        //
        // Double todayTotalUsd = todayQuoteList.stream()
        //     .map(Quote::getUsdAmount)
        //     .reduce(0D, Double::sum);

        //
        // int todayCnt = todayQuoteList.size();


        return ListResponse.toResponseEntity(SuccessCode.GET_HISTORIES, user, todayCnt, todayTotalUsd, histories);

    }
}
