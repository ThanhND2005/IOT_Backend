package com.app.common.exception;

/**
 * Exception thrown when user is not authorized or token is invalid.
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHENTICATED, message);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
