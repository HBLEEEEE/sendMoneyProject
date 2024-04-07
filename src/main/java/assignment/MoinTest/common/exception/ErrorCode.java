package assignment.MoinTest.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALIDATION_PASSWORD(BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    WRONG_IDTYPE(BAD_REQUEST, "idType이 옳지 않습니다."),
    WRONG_TARGETCURRENCY(BAD_REQUEST, "JPY 혹은 USD가 아닙니다." ),
    NEGATIVE_NUMBER(BAD_REQUEST, "음수 발생 요청입니다."),
    QUOTE_EXPIRED(BAD_REQUEST, "유효 기간이 지났습니다." ),
    LIMIT_EXCESS(BAD_REQUEST, "일일 송금 최대치를 초과합니다."),
    UNKNOWN_ERROR(BAD_REQUEST, "UNKNOWN_ERROR"),
    NOT_MATCH_EMAILFORM(BAD_REQUEST, "userID가 이메일 형식에 맞지 않습니다."),
    DUPLICATE_USER(BAD_REQUEST, "중복된 UserID입니다."),
    QUOTE_ALREADY_USED(BAD_REQUEST, "이미 처리된 견적서입니다." ),

    // 401 UNAUTHORIZED
    AUTHORIZATION(HttpStatus.UNAUTHORIZED, "수정/삭제할 수 있는 권한이 없습니다."),
    // 404 NOT_FOUND
    USER_NOT_FOUND(NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다." ),
    QUOTE_NOT_FOUND(NOT_FOUND, "해당 Quote를 찾을 수 없습니다."),
    NOT_INVALID_JWT (HttpStatus.NOT_FOUND, "유효하지 않는 JWT 서명 입니다."),
    EXPIRED_TOKEN (HttpStatus.NOT_FOUND, "만료된 JWT 토큰 입니다."),
    UNSUPPORTED_TOKEN (HttpStatus.NOT_FOUND, "지원되지 않는 JWT 토큰 입니다."),
    WRONG_TOKEN (HttpStatus.NOT_FOUND, "잘못된 JWT 토큰 입니다.."),

    // 409 CONFLICT : 현재 상태와 충돌, 중복 데이터 존재
    DUPLICATE_MEMBER(CONFLICT, "중복된 사용자가 존재합니다");

    private final HttpStatus httpStatus;
    private final String msg;
}
