package com.rently.rently.catalog.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarBrandRequest {
    @NotBlank()
    @Size(min = 3)
    String name;
}
