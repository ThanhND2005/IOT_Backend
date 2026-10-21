package com.app.modules.user.service;

import com.app.modules.user.dto.UserResponse;
import com.app.modules.user.entity.User;
import com.app.common.base.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse getById(UUID id);

    UserResponse getByUsername(String username);

    User findEntityByUsername(String username);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
