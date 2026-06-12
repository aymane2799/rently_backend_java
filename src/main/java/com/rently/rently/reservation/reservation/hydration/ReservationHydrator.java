package com.rently.rently.reservation.reservation.hydration;

import com.rently.rently.reservation.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationHydrator {

    private final ReservationHydrationResolver resolver;

    public ReservationHydrationContext hydrate(CreateReservationRequest request) {
        return ReservationHydrationContext.builder()
                .customer(resolver.resolveCustomer(request.getCustomerId()))
                .vehicle(resolver.resolveVehicle(request.getVehicleId()))
                .pickupHub(resolver.resolveHub(request.getPickupHubId(), "Pickup"))
                .returnHub(resolver.resolveHub(request.getReturnHubId(), "Return"))
                .build();
    }
}
