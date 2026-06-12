package com.rently.rently.auth.dto;

import com.rently.rently.auth.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String token;
    private String userId;
    private UserRole role;
    private String agencySlug;
    private String branchId;
}
