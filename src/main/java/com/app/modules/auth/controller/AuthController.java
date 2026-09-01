package com.app.modules.auth.controller;

import com.app.modules.auth.dto.AuthResponse;
import com.app.modules.auth.dto.LoginRequest;
import com.app.modules.auth.dto.RefreshTokenRequest;
import com.app.modules.auth.dto.RegisterRequest;
import com.app.modules.auth.service.AuthService;
import com.app.modules.user.dto.UserResponse;
import com.app.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling authentication endpoints.
 * Automatically prefixed with /api/v1 -> /api/v1/auth/*
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication, registration, token refresh and profile")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Đăng nhập bằng username hoặc email và password để nhận JWT Token")
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(BaseResponse.ok("Đăng nhập thành công", authResponse));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Đăng ký tài khoản người dùng mới vào hệ thống")
    public ResponseEntity<BaseResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return new ResponseEntity<>(BaseResponse.created("Đăng ký tài khoản thành công", authResponse), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token", description = "Sử dụng Refresh Token hợp lệ để tạo cặp token mới")
    public ResponseEntity<BaseResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(BaseResponse.ok("Làm mới token thành công", authResponse));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin tài khoản đang đăng nhập",
            description = "Yêu cầu Bearer Access Token trong Authorization Header",
            security = {@SecurityRequirement(name = "Bearer Authentication")}
    )
    public ResponseEntity<BaseResponse<UserResponse>> getCurrentUser() {
        UserResponse userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(BaseResponse.ok("Lấy thông tin tài khoản thành công", userResponse));
    }
}
