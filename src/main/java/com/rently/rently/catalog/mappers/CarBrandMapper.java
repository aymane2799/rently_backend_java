package com.rently.rently.catalog.mappers;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.requests.CarBrandRequest;
import org.springframework.stereotype.Component;

@Component
public class CarBrandMapper {
    public CarBrand toEntity(final CarBrandRequest request) {
        return CarBrand.builder()
                .name(request.getName())
                .build();
    }

    public CarBrandResponse toResponse(final CarBrand entity) {
        return CarBrandResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description("description")
                .build();
}
}