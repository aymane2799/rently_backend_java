package com.rently.rently.location.hub;

import com.rently.rently.location.branch.BranchResponse;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HubResponse {
    private String id;
    private String name;
    private HubType type;
    private String city;
    private String address;
    private boolean isActive;
    private BranchResponse branch;
    private Instant createdAt;
    private Instant updatedAt;
}
