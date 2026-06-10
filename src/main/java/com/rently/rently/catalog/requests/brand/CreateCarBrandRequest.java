package com.rently.rently.catalog.requests.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCarBrandRequest {
    @NotBlank()
    @Size(min = 3)
    String name;
}
