package com.rently.rently.catalog.mappers;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.entities.CarModel;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.reponses.CarModelResponse;
import com.rently.rently.catalog.requests.model.CreateCarModelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarModelMapper {
    final CarBrandMapper carBrandMapper;

    public CarModel toEntity(final CreateCarModelRequest request, CarBrand branc) {
        System.out.println("toEntity");

        return CarModel.builder()
                .name(request.getName())
                .category(request.getCategory())
                .brand(branc)
                .build();
    }

    public CarModelResponse toResponse(final CarModel entity) {
        System.out.println("toResponse");
        final CarBrandResponse brand = carBrandMapper.toResponse(entity.getBrand());


        return CarModelResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .brand(brand)
                .build();
}
}