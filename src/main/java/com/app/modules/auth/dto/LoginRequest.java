package com.app.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
    @Schema(description = "Tên đăng nhập hoặc email", example = "admin")
    private String usernameOrEmail;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(description = "Mật khẩu đăng nhập", example = "Admin@123")
    private String password;
}
