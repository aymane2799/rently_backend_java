package com.rently.rently.catalog.brands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBrandRequest {
    @NotBlank()
    @Size(min = 3)
    String name;
}
