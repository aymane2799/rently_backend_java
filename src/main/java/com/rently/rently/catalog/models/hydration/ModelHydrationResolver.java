package com.rently.rently.catalog.models.hydration;

import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.catalog.brands.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelHydrationResolver {
    private final BrandRepository brandRepository;

    public Brand resolveBrand(String id) {
        return id == null ? null : brandRepository.getReferenceById(id);
    }
}
