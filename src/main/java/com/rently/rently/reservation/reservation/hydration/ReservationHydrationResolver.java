package com.rently.rently.reservation.reservation.hydration;

import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.location.hub.Hub;
import com.rently.rently.location.hub.HubRepository;
import com.rently.rently.reservation.customer.Customer;
import com.rently.rently.reservation.customer.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationHydrationResolver {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final HubRepository hubRepository;

    public Customer resolveCustomer(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with id " + id + " not found!"));
    }

    public Vehicle resolveVehicle(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + id + " not found!"));
    }

    public Hub resolveHub(String id, String label) {
        return hubRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(label + " hub with id " + id + " not found!"));
    }
}
