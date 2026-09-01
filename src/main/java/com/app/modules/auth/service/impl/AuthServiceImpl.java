package com.app.modules.auth.service.impl;

import com.app.modules.auth.dto.AuthResponse;
import com.app.modules.auth.dto.LoginRequest;
import com.app.modules.auth.dto.RefreshTokenRequest;
import com.app.modules.auth.dto.RegisterRequest;
import com.app.modules.auth.service.AuthService;
import com.app.modules.user.dto.UserResponse;
import com.app.modules.user.entity.Role;
import com.app.modules.user.entity.User;
import com.app.modules.user.entity.UserStatus;
import com.app.modules.user.repository.UserRepository;
import com.app.common.exception.AppException;
import com.app.common.exception.ErrorCode;
import com.app.common.exception.ResourceNotFoundException;
import com.app.common.exception.UnauthorizedException;
import com.app.common.security.JwtService;
import com.app.common.security.SecurityUtils;
import com.app.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            if (principal.getStatus() == UserStatus.BLOCKED) {
                throw new AppException(ErrorCode.USER_INACTIVE, "Tài khoản của bạn đã bị khóa");
            }

            String accessToken = jwtService.generateAccessToken(principal);
            String refreshToken = jwtService.generateRefreshToken(principal);

            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

            log.info("User {} logged in successfully", principal.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(UserResponse.fromEntity(user))
                    .build();

        } catch (BadCredentialsException ex) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS);
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS, "Tên đăng nhập '" + request.getUsername() + "' đã tồn tại");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email '" + request.getEmail() + "' đã được sử dụng");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.create(savedUser);

        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        log.info("Registered new user with username: {}", savedUser.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserResponse.fromEntity(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateToken(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = jwtService.generateRefreshToken(principal);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.UNAUTHENTICATED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return UserResponse.fromEntity(user);
    }
}
