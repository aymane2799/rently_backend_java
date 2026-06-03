package com.rently.rently.catalog.requests;

import com.rently.rently.catalog.enums.VehicleCategory;
import com.rently.rently.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarModelRequest {
    @NotBlank()
    @Size(min = 3)
    String name;

    @NotBlank()
    @Size(min = 3)
    @ValidEnum(enumClass =  VehicleCategory.class)
    VehicleCategory category;

    @NotBlank
    @UUID
    String brandId;
}
