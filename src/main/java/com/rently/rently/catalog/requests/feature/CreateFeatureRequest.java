package com.rently.rently.catalog.requests.feature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFeatureRequest {
    @NotBlank()
    @Size(min = 3)
    String name;

    private String icon;

    private String description;

}
