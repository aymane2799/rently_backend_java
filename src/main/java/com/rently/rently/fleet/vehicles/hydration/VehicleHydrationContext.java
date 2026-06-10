package com.rently.rently.fleet.vehicles.hydration;

import com.rently.rently.catalog.features.Feature;
import com.rently.rently.catalog.models.Model;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Builder
@Getter
@Setter
public class VehicleHydrationContext {
    private Model model;
    private Set<Feature> features;
}
