package org.pagebyfeel.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}