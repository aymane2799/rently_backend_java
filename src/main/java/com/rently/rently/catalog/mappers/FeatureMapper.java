package com.rently.rently.catalog.mappers;

import com.rently.rently.catalog.entities.Feature;
import com.rently.rently.catalog.reponses.FeatureResponse;
import com.rently.rently.catalog.requests.feature.CreateFeatureRequest;
import com.rently.rently.shared.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureMapper implements Mapper<Feature, CreateFeatureRequest, FeatureResponse> {

    @Override
    public Feature toEntity(CreateFeatureRequest request) {
        return Feature.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .description(request.getDescription())
                .build();
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
