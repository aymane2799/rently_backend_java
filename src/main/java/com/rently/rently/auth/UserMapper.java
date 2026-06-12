package com.rently.rently.auth;

import com.rently.rently.auth.dto.UserResponse;
import com.rently.rently.shared.mappers.ResponseMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements ResponseMapper<User, UserResponse> {

    @Override
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .agencySlug(user.getAgencySlug())
                .branchId(user.getBranchId())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
