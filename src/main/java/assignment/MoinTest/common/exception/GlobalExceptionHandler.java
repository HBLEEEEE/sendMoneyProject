package assignment.MoinTest.common.exception;


import assignment.MoinTest.Response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> exampleResponseValidation(MethodArgumentNotValidException e){

        BindingResult result = e.getBindingResult();
        String msg = result.getFieldError().getDefaultMessage();

        return ResponseEntity
                .status(e.getStatusCode())
                .body(BaseResponse.builder()
                        .resultCode(e.getStatusCode().value())
                        .resultMsg(msg)
                        .build());
    }

}
