package com.app.modules.user.controller;

import com.app.modules.auth.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAdminToken() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("admin")
                .password("Admin@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        return objectMapper.readTree(jsonResponse).path("data").path("accessToken").asText();
    }

    private String getUserToken() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("user")
                .password("User@123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        return objectMapper.readTree(jsonResponse).path("data").path("accessToken").asText();
    }

    @Test
    @DisplayName("GET /api/v1/users - ADMIN có thể lấy danh sách người dùng")
    void testGetAllUsersAsAdmin() throws Exception {
        String adminToken = getAdminToken();

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/v1/users - USER bình thường bị từ chối 403 Forbidden")
    void testGetAllUsersAsUserForbidden() throws Exception {
        String userToken = getUserToken();

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.code", is(403)));
    }
}
