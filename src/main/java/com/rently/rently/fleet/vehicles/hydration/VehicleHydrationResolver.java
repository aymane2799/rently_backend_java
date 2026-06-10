package com.rently.rently.fleet.vehicles.hydration;

import com.rently.rently.catalog.features.Feature;
import com.rently.rently.catalog.features.FeatureRepository;
import com.rently.rently.catalog.models.Model;
import com.rently.rently.catalog.models.ModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class VehicleHydrationResolver {
    private final ModelRepository modelRepository;
    private final FeatureRepository featureRepository;

    public Model resolveModel(String id) {
        if (id == null) return null;
        return modelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Model with id " + id + " not found!"));
    }

    public Set<Feature> resolveFeatures(Set<String> ids) {
        if (ids == null) return null;
        if (ids.isEmpty()) return Set.of();
        return new HashSet<>(featureRepository.findAllById(ids));
    }
}
