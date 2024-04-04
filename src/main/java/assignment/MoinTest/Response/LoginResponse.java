package assignment.MoinTest.Response;

import assignment.MoinTest.common.exception.ErrorCode;
import assignment.MoinTest.common.exception.SuccessCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class LoginResponse {


    private final int resultCode;
    private final String resultMsg;
    private final String token;

    public static ResponseEntity<LoginResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(LoginResponse.builder()
                        .resultCode(errorCode.getHttpStatus().value())
                        .resultMsg(errorCode.getMsg())
                        .build());
    }

    public static ResponseEntity<LoginResponse> toResponseEntity(SuccessCode successCode, String token) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .header("Content-Type", "application/json;charset=UTF-8" )
                .body(LoginResponse.builder()
                        .resultCode(successCode.getHttpStatus().value())
                        .resultMsg(successCode.getDetail())
                        .token(token)
                        .build());
    }
}
