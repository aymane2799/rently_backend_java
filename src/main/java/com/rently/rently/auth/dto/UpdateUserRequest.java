package com.rently.rently.auth.dto;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String password;
    private String branchId;
    private Boolean active;
}
