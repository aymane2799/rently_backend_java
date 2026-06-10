package com.rently.rently.catalog.models;

import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.catalog.brands.BrandMapper;
import com.rently.rently.catalog.brands.BrandResponse;
import com.rently.rently.catalog.models.hydration.ModelHydrationContext;
import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelMapper implements
        ResponseMapper<Model, ModelResponse>,
        CreateMapper<Model, CreateModelRequest, ModelHydrationContext>,
        PatchMapper<Model, UpdateModelRequest, ModelHydrationContext>
{
    final BrandMapper brandMapper;

    @Override
    public Model toEntity(final CreateModelRequest request, ModelHydrationContext ctx) {

        return Model.builder()
                .name(request.getName())
                .category(request.getCategory())
                .brand(ctx.getBrand())
                .build();
    }

    @Override
    public Model patchEntity(Model entity, UpdateModelRequest request, ModelHydrationContext ctx) {
        Model model = new Model();

        if (request.getName() != null) {
            model.setName(request.getName());
        }

        if (ctx.getBrand() != null) {
            model.setBrand(ctx.getBrand());
        }

        return model;
    }

    @Override
    public ModelResponse toResponse(final Model entity) {
        final BrandResponse brand = brandMapper.toResponse(entity.getBrand());


        return ModelResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .brand(brand)
                .build();
}
}