package com.rently.rently.location.branch;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BranchResponse {
    private String id;
    private String name;
    private String city;
    private String address;
    private String phone;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
