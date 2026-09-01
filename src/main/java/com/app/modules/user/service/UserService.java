package com.app.modules.user.service;

import com.app.modules.user.dto.UserResponse;
import com.app.modules.user.entity.User;
import com.app.common.base.PageResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getById(Long id);

    UserResponse getByUsername(String username);

    User findEntityByUsername(String username);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
