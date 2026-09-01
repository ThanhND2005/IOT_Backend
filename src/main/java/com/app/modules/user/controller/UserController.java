package com.app.modules.user.controller;

import com.app.common.base.BaseResponse;
import com.app.common.base.PageResponse;
import com.app.modules.user.dto.UserResponse;
import com.app.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Lấy danh sách người dùng phân trang",
            description = "Chỉ ADMIN có quyền truy cập",
            security = {@SecurityRequirement(name = "Bearer Authentication")}
    )
    public ResponseEntity<BaseResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(BaseResponse.ok("Lấy danh sách người dùng thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Lấy chi tiết người dùng theo ID",
            security = {@SecurityRequirement(name = "Bearer Authentication")}
    )
    public ResponseEntity<BaseResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(BaseResponse.ok("Lấy thông tin người dùng thành công", response));
    }
}