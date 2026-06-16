package com.rently.rently.agency;

import com.rently.rently.agency.dto.PublicVehicleResponse;
import com.rently.rently.catalog.PublicCatalogService;
import com.rently.rently.catalog.features.Feature;
import com.rently.rently.catalog.features.FeatureMapper;
import com.rently.rently.catalog.features.FeatureResponse;
import com.rently.rently.catalog.models.Model;
import com.rently.rently.fleet.vehicles.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PublicVehicleMapper {

    private final PublicCatalogService publicCatalogService;
    private final FeatureMapper featureMapper;

    public PublicVehicleResponse toResponse(Vehicle vehicle, String primaryImageUrl) {
        Model model = publicCatalogService.getModelWithBrand(vehicle.getModelId());

        Set<FeatureResponse> features = Set.of();
        if (vehicle.getFeatureIds() != null && !vehicle.getFeatureIds().isEmpty()) {
            List<Feature> featureList = publicCatalogService.getFeatures(vehicle.getFeatureIds());
            features = featureList.stream()
                    .map(featureMapper::toResponse)
                    .collect(Collectors.toSet());
        }

        return PublicVehicleResponse.builder()
                .id(vehicle.getId())
                .modelName(model.getName())
                .brandName(model.getBrand().getName())
                .category(model.getCategory())
                .transmission(vehicle.getTransmission())
                .fuelType(vehicle.getFuelType())
                .dailyBaseRate(vehicle.getDailyBaseRate())
                .seats(vehicle.getSeats())
                .year(vehicle.getYear())
                .color(vehicle.getColor())
                .features(features)
                .primaryImageUrl(primaryImageUrl)
                .build();
    }
}
