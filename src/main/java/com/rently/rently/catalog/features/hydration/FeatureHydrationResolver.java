package com.rently.rently.catalog.features.hydration;

import com.rently.rently.catalog.features.Feature;
import com.rently.rently.catalog.features.FeatureRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FeatureHydrationResolver {
    final FeatureRepository repository;

    public String resolveName(String id, String name){
        if (name == null) return null;

        final Optional<Feature> existingBrand = repository.findByName(name);

        if (existingBrand.isPresent() && !existingBrand.get().getId().equals(id)) {
            throw new EntityExistsException("Feature with name " + name + " already exists!");
        }

        return name;
    }
}
