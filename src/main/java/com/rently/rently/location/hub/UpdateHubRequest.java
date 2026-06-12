package com.rently.rently.location.hub;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateHubRequest {

    @Size(max = 150)
    private String name;

    private HubType type;
    private String city;
    private String address;
}
