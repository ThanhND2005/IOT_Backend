package com.app.modules.auth.controller;

import com.app.modules.auth.dto.LoginRequest;
import com.app.modules.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/auth/login - Đăng nhập thành công với tài khoản mặc định")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("Admin@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.code", is(200)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.username", is("admin")))
                .andExpect(jsonPath("$.data.user.role", is("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Đăng nhập thất bại do sai mật khẩu")
    void testLoginFailedWrongPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is(400)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Đăng ký tài khoản mới thành công")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser123")
                .password("Password@123")
                .email("newuser123@example.com")
                .fullName("New Test User")
                .phoneNumber("0988776655")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.code", is(201)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.username", is("newuser123")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Lấy thông tin user hiện tại qua JWT")
    void testGetCurrentUserWithToken() throws Exception {
        // 1. Login first to get access token
        LoginRequest loginRequest = LoginRequest.builder()
                .usernameOrEmail("user")
                .password("User@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(jsonResponse).path("data").path("accessToken").asText();

        // 2. Call /api/v1/auth/me with Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("user")))
                .andExpect(jsonPath("$.data.role", is("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Truy cập không có token trả về 401")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is(401)));
    }
}
