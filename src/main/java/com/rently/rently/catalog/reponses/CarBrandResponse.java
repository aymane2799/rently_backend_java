package com.rently.rently.catalog.reponses;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarBrandResponse {
    private String id;
    private String name;
    private String description;
}
