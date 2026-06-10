package com.rently.rently.catalog.requests.model;

import com.rently.rently.catalog.enums.VehicleCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCarModelRequest {
    String name;

    VehicleCategory category;

    @UUID
    String brandId;
}
