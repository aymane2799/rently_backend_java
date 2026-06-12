package com.rently.rently.catalog.features;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeatureResponse {
    private String id;
    private String name;
    private String icon;
    private String description;
    private boolean isActive;
}
