package com.app.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base application exception.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public AppException(String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = ErrorCode.INVALID_REQUEST;
        this.httpStatus = httpStatus;
    }
}
