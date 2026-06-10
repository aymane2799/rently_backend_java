package com.rently.rently.catalog.brands;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBrandRequest {
    @Size(min = 3)
    String name;
}
