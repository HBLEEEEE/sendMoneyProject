package assignment.MoinTest.Response;


import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import assignment.MoinTest.transfer.dto.QuoteResponseDto;
import assignment.MoinTest.transfer.entity.Quote;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class QuoteResponse {

    private final int resultCode;
    private final String resultMsg;
    private final QuoteResponseDto quote;

    public static ResponseEntity<QuoteResponse> toResponseEntity(SuccessCode successCode, Quote quote) {
        QuoteResponseDto quoteResponseDto = new QuoteResponseDto();
        quoteResponseDto.setQuoteId(quote.getQuoteId());
        quoteResponseDto.setExchangeRate(quote.getExchangeRate());
        quoteResponseDto.setExpireTime(quote.getRequestedAt());
        quoteResponseDto.setTargetAmount(quote.getTargetAmount());

        return ResponseEntity
                .status(successCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(QuoteResponse.builder()
                        .resultCode(successCode.getHttpStatus().value())
                        .resultMsg(successCode.getDetail())
                        .quote(quoteResponseDto)
                        .build());
    }

    public static ResponseEntity<QuoteResponse> toResponseEntity(ErrorCode errorCode){
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(QuoteResponse.builder()
                        .resultCode(errorCode.getHttpStatus().value())
                        .resultMsg(errorCode.getMsg())
                        .build());
    }




}
