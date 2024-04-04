package assignment.MoinTest.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    SIGNUP_SUCCESS(HttpStatus.OK, "OK"),
    LOGIN_SUCCESS(HttpStatus.OK, "OK"),
    SAVE_QUOTE(HttpStatus.OK, "OK" ),
    REQUEST_QUOTE(HttpStatus.OK, "OK" ),
    GET_HISTORIES(HttpStatus.OK, "OK" );

    private final HttpStatus httpStatus;
    private final String detail;
}
