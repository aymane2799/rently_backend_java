package com.rently.rently.catalog.mappers;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.entities.CarModel;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.reponses.CarModelResponse;
import com.rently.rently.catalog.requests.CarBrandRequest;
import com.rently.rently.catalog.requests.CarModelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarModelMapper {
    final CarBrandMapper carBrandMapper;

    public CarModel toEntity(final CarModelRequest request) {
        return CarModel.builder()
                .name(request.getName())
                .category(request.getCategory())
                .brandId(request.getBrandId())
                .build();
    }

    public CarModelResponse toResponse(final CarModel entity) {
        return CarModelResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .brand(this.carBrandMapper.toResponse(entity.getBrand()))
                .build();
}
}