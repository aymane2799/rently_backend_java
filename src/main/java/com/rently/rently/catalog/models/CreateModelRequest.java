package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateModelRequest {
    @NotBlank()
    String name;

    VehicleCategory category;

    @NotBlank
    @UUID
    String brandId;
}
