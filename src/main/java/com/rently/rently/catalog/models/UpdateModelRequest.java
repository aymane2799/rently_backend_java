package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateModelRequest {
    String name;

    VehicleCategory category;

    @UUID
    String brandId;
}
