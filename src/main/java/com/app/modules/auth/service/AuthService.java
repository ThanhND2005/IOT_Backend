package com.app.modules.auth.service;

import com.app.modules.auth.dto.AuthResponse;
import com.app.modules.auth.dto.LoginRequest;
import com.app.modules.auth.dto.RefreshTokenRequest;
import com.app.modules.auth.dto.RegisterRequest;
import com.app.modules.user.dto.UserResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserResponse getCurrentUser();
}
