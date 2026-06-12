package com.rently.rently.catalog.features;


import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureMapper implements
        ResponseMapper<Feature, FeatureResponse>,
        CreateMapper<Feature, CreateFeatureRequest, Object>,
        PatchMapper<Feature, UpdateFeatureRequest, Object> {

    @Override
    public Feature toEntity(CreateFeatureRequest request,  Object ctx) {
        return Feature.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .description(request.getDescription())
                .build();
    }

    @Override
    public void patchEntity(Feature entity, UpdateFeatureRequest request, Object ctx) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getIcon() != null) {
            entity.setIcon(request.getIcon());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
    }

    @Override
    public FeatureResponse toResponse(Feature feature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .name(feature.getName())
                .icon(feature.getIcon())
                .description(feature.getDescription())
                .isActive(feature.isActive())
                .build();
    }
}
