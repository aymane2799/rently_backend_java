package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.catalog.brands.BrandResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelResponse {
    private String id;
    private String name;
    private VehicleCategory category;
    private BrandResponse brand;
}
