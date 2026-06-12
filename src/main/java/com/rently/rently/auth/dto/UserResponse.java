package com.rently.rently.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rently.rently.auth.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private String agencySlug;
    private String branchId;
    @JsonProperty("isActive")
    private boolean active;
    private Instant createdAt;
}
