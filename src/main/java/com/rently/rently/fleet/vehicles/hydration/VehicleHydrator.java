package com.rently.rently.fleet.vehicles.hydration;

import com.rently.rently.fleet.vehicles.CreateVehicleRequest;
import com.rently.rently.fleet.vehicles.UpdateVehicleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleHydrator {

    private final VehicleHydrationResolver resolver;

    public VehicleHydrationContext hydrate(CreateVehicleRequest request) {
        resolver.validateHubExists(request.getCurrentHubId());
        return VehicleHydrationContext.builder().build();
    }

    public VehicleHydrationContext hydrate(UpdateVehicleRequest request) {
        resolver.validateHubExists(request.getCurrentHubId());
        return VehicleHydrationContext.builder().build();
    }
}
