package com.rently.rently.catalog.models.hydration;

import com.rently.rently.catalog.models.CreateModelRequest;
import com.rently.rently.catalog.models.UpdateModelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelHydrator {
    final ModelHydrationResolver resolver;

    public ModelHydrationContext hydrate(CreateModelRequest request) {

        return ModelHydrationContext.builder()
                .brand(resolver.resolveBrand(request.getBrandId()))
                .build();

    }

    public ModelHydrationContext hydrate(UpdateModelRequest request) {

        return ModelHydrationContext.builder()
                .brand(resolver.resolveBrand(request.getBrandId()))
                .build();
    }
}
