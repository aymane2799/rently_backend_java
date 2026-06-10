package com.rently.rently.catalog.features.hydration;

import com.rently.rently.catalog.models.CreateModelRequest;
import com.rently.rently.catalog.models.UpdateModelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureHydrator {
    final FeatureHydrationResolver resolver;

    public FeatureHydrationContext hydrate(CreateModelRequest request) {

        return FeatureHydrationContext.builder()
                .build();

    }

    public FeatureHydrationContext hydrate(UpdateModelRequest request) {

        return FeatureHydrationContext.builder()
                .build();
    }
}
