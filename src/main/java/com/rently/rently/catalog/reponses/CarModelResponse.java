package com.rently.rently.catalog.reponses;

import com.rently.rently.catalog.enums.VehicleCategory;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarModelResponse {
    private String id;
    private String name;
    private VehicleCategory category;
    private CarBrandResponse brand;
}
