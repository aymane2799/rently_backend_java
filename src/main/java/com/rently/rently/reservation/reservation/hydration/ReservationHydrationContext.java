package com.rently.rently.reservation.reservation.hydration;

import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.location.hub.Hub;
import com.rently.rently.reservation.customer.Customer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ReservationHydrationContext {
    private Customer customer;
    private Vehicle vehicle;
    private Hub pickupHub;
    private Hub returnHub;
}
