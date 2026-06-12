package com.rently.rently.fleet.vehicles.hydration;

import com.rently.rently.location.hub.HubRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleHydrationResolver {

    private final HubRepository hubRepository;

    public void validateHubExists(String hubId) {
        if (hubId != null && !hubRepository.existsById(hubId)) {
            throw new EntityNotFoundException("Hub not found: " + hubId);
        }
    }
}
