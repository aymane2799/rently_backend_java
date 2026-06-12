package com.rently.rently.catalog.brands;

import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper
        implements
        ResponseMapper<Brand, BrandResponse>,
        CreateMapper<Brand, CreateBrandRequest, Object>,
        PatchMapper<Brand, UpdateBrandRequest, Object> {
    @Override
    public Brand toEntity(final CreateBrandRequest request, Object ctx) {
        return Brand.builder()
                .name(request.getName())
                .build();
    }

    @Override
    public void patchEntity(Brand entity, UpdateBrandRequest request, Object ctx) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
    }

    @Override
    public BrandResponse toResponse(final Brand entity) {
        return BrandResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isActive(entity.isActive())
                .build();
    }
}