 package com.rently.rently.catalog.features;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateFeatureRequest {
    @Size(min = 3)
    String name;

    private String icon;

    private String description;

}
