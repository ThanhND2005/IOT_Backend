package com.app.common.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all REST endpoints.
 *
 * @param <T> Response payload data type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    @Builder.Default
    private boolean success = true;

    private int code;

    private String message;

    private T data;

    private Object errors;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    // ==================== Factory Methods for Success ====================

    public static <T> BaseResponse<T> ok() {
        return BaseResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Thành công")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Thành công")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> ok(String message, T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> created(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message("Tạo mới thành công")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> created(String message, T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== Factory Methods for Failure ====================

    public static <T> BaseResponse<T> error(int code, String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> error(int code, String message, Object errors) {
        return BaseResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> BaseResponse<T> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST.value(), message);
    }

    public static <T> BaseResponse<T> badRequest(String message, Object errors) {
        return error(HttpStatus.BAD_REQUEST.value(), message, errors);
    }

    public static <T> BaseResponse<T> unauthorized(String message) {
        return error(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public static <T> BaseResponse<T> forbidden(String message) {
        return error(HttpStatus.FORBIDDEN.value(), message);
    }

    public static <T> BaseResponse<T> notFound(String message) {
        return error(HttpStatus.NOT_FOUND.value(), message);
    }

    public static <T> BaseResponse<T> internalServerError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
    }
}
