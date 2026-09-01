package com.app.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Standard business error codes across all modules.
 */
@Getter
public enum ErrorCode {

    // General & System
    UNCATEGORIZED_EXCEPTION("SYS_001", "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY("SYS_002", "Khóa dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("SYS_003", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("SYS_004", "Không tìm thấy tài nguyên yêu cầu", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("SYS_005", "Phương thức HTTP không được hỗ trợ", HttpStatus.METHOD_NOT_ALLOWED),

    // Authentication & Authorization
    UNAUTHENTICATED("AUTH_001", "Chưa xác thực, vui lòng đăng nhập", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("AUTH_002", "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    BAD_CREDENTIALS("AUTH_003", "Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("AUTH_004", "Phiên đăng nhập đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_005", "Mã xác thực không hợp lệ", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("AUTH_006", "Refresh token đã hết hạn, vui lòng đăng nhập lại", HttpStatus.UNAUTHORIZED),

    // User Module
    USER_NOT_FOUND("USER_001", "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_002", "Tên người dùng đã tồn tại", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS("USER_003", "Email đã được sử dụng", HttpStatus.CONFLICT),
    USER_INACTIVE("USER_004", "Tài khoản người dùng đã bị khóa hoặc chưa kích hoạt", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
