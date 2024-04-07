package assignment.MoinTest.transfer.service;


import assignment.MoinTest.Response.BaseResponse;
import assignment.MoinTest.Response.ListResponse;
import assignment.MoinTest.Response.QuoteResponse;
import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import assignment.MoinTest.security.UserDetailsImpl;
import assignment.MoinTest.transfer.dto.HistoryResponseDto;
import assignment.MoinTest.transfer.dto.QuoteRequestDto;
import assignment.MoinTest.transfer.dto.RequestDto;
import assignment.MoinTest.transfer.entity.FeeRateAmount;
import assignment.MoinTest.transfer.entity.Quote;
import assignment.MoinTest.transfer.entity.Request;
import assignment.MoinTest.transfer.entity.TargetCurrencyTypeEnum;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Transactional
    public ResponseEntity<QuoteResponse> quote(QuoteRequestDto quoteRequestDto, UserDetailsImpl userDetails){
        User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
        if (user == null){
            return QuoteResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        //0원 미만의 요청 예외 처리
        if(quoteRequestDto.getAmount()<0){
            return QuoteResponse.toResponseEntity(ErrorCode.NEGATIVE_NUMBER);
        }

        String targetCurrency = quoteRequestDto.getTargetCurrency();
        //JPY 또는 USD만 취급
        TargetCurrencyTypeEnum currencyType = TargetCurrencyTypeEnum.findByCurrencyType(quoteRequestDto.getTargetCurrency());
        if(currencyType == null){
            return QuoteResponse.toResponseEntity(ErrorCode.WRONG_TARGETCURRENCY);
        }

        FeeRateAmount feeRateAmount = CurrencyServiceFactory.calculate(currencyType, quoteRequestDto.getAmount());
        double fee = feeRateAmount.getFee();
        double usdSendAmount = feeRateAmount.getUsdSendAmount();
        double targetAmount = feeRateAmount.getTargetAmount();
        double exchangeRate = feeRateAmount.getTargetRate();
        double usdRate = feeRateAmount.getUsdRate();

        //보낼 금액 음수인 경우 예외 처리
        if(targetAmount<0){
            return QuoteResponse.toResponseEntity(ErrorCode.NEGATIVE_NUMBER);
        }

        Quote quote = new Quote();
        quote.setSourceAmount(quoteRequestDto.getAmount());
        quote.setFee(fee);
        quote.setUseExchangeRate(usdRate);
        quote.setUsdAmount(usdSendAmount);
        quote.setTargetCurrency(targetCurrency);
        quote.setExchangeRate(exchangeRate);
        quote.setTargetAmount(targetAmount);
        quote.setRequestedAt(LocalDateTime.now());
        quote.setUser(user);

        quoteRepository.save(quote);

        return QuoteResponse.toResponseEntity(SuccessCode.SAVE_QUOTE, quote);
    }

    @Transactional
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public ResponseEntity<BaseResponse> request(RequestDto requestDto, UserDetailsImpl userDetails) {

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

        if (Math.abs(duration.toMinutes()) > 10){
            return BaseResponse.toResponseEntity(ErrorCode.QUOTE_EXPIRED);
        }

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘 날짜의 시작 시간
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); // 오늘 날짜의 종료 시간
        List<Request> requests = requestRepository.findByUserAndRequestTimeBetweenOrderByRequestTimeDesc(
                user, startOfToday, endOfToday
        );

        double todaySendTotal = requests.stream()
                .map(Request::getQuote)
                .map(Quote::getUsdAmount)
                .reduce(0D, Double::sum);
        todaySendTotal += quote.getUsdAmount();

        if (user.getIdType() == UserIdTypeEnum.REG_NO && todaySendTotal >= 1000){
            return BaseResponse.toResponseEntity(ErrorCode.LIMIT_EXCESS);
        } else if (user.getIdType() == UserIdTypeEnum.BUSINESS_NO && todaySendTotal >= 5000) {
            return BaseResponse.toResponseEntity(ErrorCode.LIMIT_EXCESS);
        }

        Request request = new Request();
        request.setQuote(quote);
        request.setUser(user);
        request.setRequestTime(now);
        requestRepository.save(request);
        quote.setRequest(request);

        return BaseResponse.toResponseEntity(SuccessCode.REQUEST_QUOTE);
    }


    @Transactional
    public ResponseEntity<ListResponse> list(UserDetailsImpl userDetails) {

        User user = userRepository.findByUserId(userDetails.getUser().getUserId()).orElse(null);
        if (user==null){
            return ListResponse.toResponseEntity(ErrorCode.USER_NOT_FOUND);
        }

        List<Quote> quotes = quoteRepository.findAllByUserOrderByRequestedAt(user);
        List<HistoryResponseDto> histories = quotes.stream()
                .filter(history -> history.getRequest() != null)
                .map(history -> {
                    HistoryResponseDto historyResponseDto = new HistoryResponseDto();
                    historyResponseDto.setSourceAmount(history.getSourceAmount());
                    historyResponseDto.setFee(history.getFee());
                    historyResponseDto.setUsdExchangeRate(history.getUseExchangeRate());
                    historyResponseDto.setUsdAmount(history.getUsdAmount());
                    historyResponseDto.setTargetCurrency(history.getTargetCurrency());
                    historyResponseDto.setExchangeRate(history.getExchangeRate());
                    historyResponseDto.setRequestedDate(history.getRequestedAt().format(formatter));
                    historyResponseDto.setTargetAmount(history.getTargetAmount());

                    return historyResponseDto;
                })
                .toList();

        LocalDate nowDate = LocalDateTime.now().toLocalDate();
        List<Quote> todayQuoteList = quotes.stream()
                .filter(quote -> quote.getRequest() != null)
                .filter(quote -> nowDate.isEqual(quote.getRequestedAt().toLocalDate()))
                .toList();

        int todayCnt = todayQuoteList.size();
        double todayTotalUsd = todayQuoteList.stream()
                .filter(quote -> quote.getRequest() != null)
                .map(Quote::getUsdAmount)
                .reduce(0D, Double::sum);

        return ListResponse.toResponseEntity(SuccessCode.GET_HISTORIES, user, todayCnt, todayTotalUsd, histories);

    }
}
