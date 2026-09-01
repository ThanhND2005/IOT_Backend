package com.app.modules.auth.dto;

import com.app.modules.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả xác thực người dùng")
public class AuthResponse {

    @Schema(description = "Access Token dạng JWT")
    private String accessToken;

    @Schema(description = "Refresh Token dạng JWT")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Loại Token", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian hết hạn của Access Token (mili giây)", example = "86400000")
    private long expiresIn;

    @Schema(description = "Thông tin chi tiết của người dùng đã đăng nhập")
    private UserResponse user;
}
