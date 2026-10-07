package com.app.modules.user.controller;

import com.app.common.base.BaseResponse;
import com.app.modules.auth.service.AuthService;
import com.app.modules.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user profile endpoint according to SRS Section 3.4.1:
 * GET /api/v1/profile/me
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Profile Management", description = "APIs for user profile and document links")
public class ProfileController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(
            summary = "Lấy thông tin cá nhân của người dùng",
            description = "Trả về thông tin cá nhân và danh sách liên kết tài liệu dự án",
            security = {@SecurityRequirement(name = "Bearer Authentication")}
    )
    public ResponseEntity<BaseResponse<UserResponse>> getMyProfile() {
        UserResponse userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(BaseResponse.ok("Lấy thông tin cá nhân thành công", userResponse));
    }
}
