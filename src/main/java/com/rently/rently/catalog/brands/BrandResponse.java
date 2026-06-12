package com.rently.rently.catalog.brands;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandResponse {
    private String id;
    private String name;
    private boolean isActive;
}
