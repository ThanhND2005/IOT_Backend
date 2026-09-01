package com.app.common.security;

import com.app.modules.user.entity.Role;
import com.app.modules.user.entity.User;
import com.app.modules.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .username("testjwtuser")
                .password("Password@123")
                .email("testjwt@example.com")
                .fullName("Test JWT User")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(99L);

        userPrincipal = UserPrincipal.create(user);
    }

    @Test
    @DisplayName("Tạo Access Token và xác thực thành công")
    void testGenerateAndValidateAccessToken() {
        String token = jwtService.generateAccessToken(userPrincipal);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));

        String username = jwtService.extractUsername(token);
        assertEquals("testjwtuser", username);
        assertTrue(jwtService.isTokenValid(token, userPrincipal));
    }

    @Test
    @DisplayName("Tạo Refresh Token và trích xuất username")
    void testGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);
        assertNotNull(refreshToken);
        assertTrue(jwtService.validateToken(refreshToken));
        assertEquals("testjwtuser", jwtService.extractUsername(refreshToken));
    }
}
