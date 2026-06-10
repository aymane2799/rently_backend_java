package com.rently.rently.catalog.mappers;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.requests.brand.CreateCarBrandRequest;
import com.rently.rently.shared.Mapper;
import org.springframework.stereotype.Component;

@Component
public class CarBrandMapper implements Mapper<CarBrand, CreateCarBrandRequest, CarBrandResponse> {
    public CarBrand toEntity(final CreateCarBrandRequest request) {
        return CarBrand.builder()
                .name(request.getName())
                .build();
    }

    public CarBrandResponse toResponse(final CarBrand entity) {
        return CarBrandResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
}
}