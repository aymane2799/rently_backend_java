package com.rently.rently.location.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateHubRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotNull
    private HubType type;

    @NotBlank
    private String city;

    private String address;
}
