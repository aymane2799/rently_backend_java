package com.rently.rently.auth;

import com.rently.rently.auth.dto.RegisterUserRequest;
import com.rently.rently.auth.dto.UpdateUserRequest;
import com.rently.rently.auth.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAll(String agencySlug);
    UserResponse get(String id);
    UserResponse create(RegisterUserRequest request, String agencySlug);
    void update(String id, UpdateUserRequest request);
    void delete(String id);
}
