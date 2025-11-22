package org.pagebyfeel.exception;

import lombok.Getter;
import org.pagebyfeel.exception.user.UserErrorCode;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(UserErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}