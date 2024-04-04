package assignment.MoinTest.Response;

import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import assignment.MoinTest.transfer.entity.Quote;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class BaseResponse {


    private final int resultCode;
    private final String resultMsg;

    public static ResponseEntity<BaseResponse> toResponseEntity(ErrorCode errorCode){
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(BaseResponse.builder()
                        .resultCode(errorCode.getHttpStatus().value())
                        .resultMsg(errorCode.getMsg())
                        .build());
    }

    public static ResponseEntity<BaseResponse> toResponseEntity(SuccessCode successCode) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(BaseResponse.builder()
                        .resultCode(successCode.getHttpStatus().value())
                        .resultMsg(successCode.getDetail())
                        .build());
    }
}
