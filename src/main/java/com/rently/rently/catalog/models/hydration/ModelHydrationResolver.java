package com.rently.rently.catalog.models.hydration;

import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.catalog.brands.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelHydrationResolver {
    private final BrandRepository brandRepository;

    public Brand resolveBrand(String id) {
        if (id == null) return null;
        return brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id " + id + " not found!"));
    }
}
