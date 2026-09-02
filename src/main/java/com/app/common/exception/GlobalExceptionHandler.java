package com.app.common.exception;

import com.app.common.base.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler intercepting all controller exceptions
 * and returning standardized BaseResponse.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle custom AppException
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResponse<Object>> handleAppException(AppException ex) {
        log.warn("Business application exception: {}", ex.getMessage());
        BaseResponse<Object> response = BaseResponse.error(
                ex.getHttpStatus().value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    /**
     * Handle DeviceTimeoutException (IoT device not responding)
     */
    @ExceptionHandler(DeviceTimeoutException.class)
    public ResponseEntity<BaseResponse<Object>> handleDeviceTimeoutException(DeviceTimeoutException ex) {
        log.warn("Device timeout: {}", ex.getMessage());
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.GATEWAY_TIMEOUT);
    }

    /**
     * Handle @Valid validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        BaseResponse<Object> response = BaseResponse.builder()
                .success(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu đầu vào không hợp lệ")
                .errors(errors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle BadCredentialsException (incorrect login credentials)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials error: {}", ex.getMessage());
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.BAD_CREDENTIALS.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Spring Security AccessDeniedException (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied error: {}", ex.getMessage());
        BaseResponse<Object> response = BaseResponse.forbidden(
                ErrorCode.UNAUTHORIZED.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle Spring Security AuthenticationException (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        BaseResponse<Object> response = BaseResponse.unauthorized(
                ErrorCode.UNAUTHENTICATED.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle HTTP Method Not Supported (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        BaseResponse<Object> response = BaseResponse.error(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Phương thức " + ex.getMethod() + " không được hỗ trợ cho đường dẫn này"
        );
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle Resource Not Found (404)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleNoResourceFound(NoResourceFoundException ex) {
        BaseResponse<Object> response = BaseResponse.notFound(
                "Không tìm thấy tài nguyên: " + ex.getResourcePath()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle general/uncaught exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleGeneralException(Exception ex) {
        log.error("Internal Server Error occurred: ", ex);
        BaseResponse<Object> response = BaseResponse.internalServerError(
                ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
