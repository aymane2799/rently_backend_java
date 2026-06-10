package com.rently.rently.catalog.features;


import com.rently.rently.catalog.features.hydration.FeatureHydrationContext;
import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureMapper implements
        ResponseMapper<Feature, FeatureResponse>,
        CreateMapper<Feature, CreateFeatureRequest, FeatureHydrationContext>,
        PatchMapper<Feature, UpdateFeatureRequest, FeatureHydrationContext> {

    @Override
    public Feature toEntity(CreateFeatureRequest request,  FeatureHydrationContext ctx) {
        return Feature.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .description(request.getDescription())
                .build();
    }

    @Override
    public Feature patchEntity (Feature entity, UpdateFeatureRequest request,  FeatureHydrationContext ctx) {
        Feature feature = new Feature();

        if(request.getName() != null) {
            feature.setName(request.getName());
        }

        if(request.getIcon() != null) {
            feature.setIcon(request.getIcon());
        }

        if(request.getDescription() != null) {
            feature.setDescription(request.getDescription());
        }
        
        return feature;
    }

    @Override
    public FeatureResponse toResponse(Feature feature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .name(feature.getName())
                .icon(feature.getIcon())
                .description(feature.getDescription())
                .build();
    }
}
