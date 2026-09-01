package com.app.modules.user.dto;

import com.app.modules.user.entity.Role;
import com.app.modules.user.entity.User;
import com.app.modules.user.entity.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String studentCode;
    private String avatarUrl;
    private String githubUrl;
    private String figmaUrl;
    private String systemDocUrl;
    private String apiDocUrl;
    private Role role;
    private UserStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .studentCode(user.getStudentCode())
                .avatarUrl(user.getAvatarUrl())
                .githubUrl(user.getGithubUrl())
                .figmaUrl(user.getFigmaUrl())
                .systemDocUrl(user.getSystemDocUrl())
                .apiDocUrl(user.getApiDocUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
