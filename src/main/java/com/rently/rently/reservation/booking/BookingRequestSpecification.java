package com.rently.rently.reservation.booking;

import com.rently.rently.fleet.vehicles.Vehicle;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class BookingRequestSpecification {

    private BookingRequestSpecification() {}

    public static Specification<BookingRequest> withFilters(BookingRequestStatus status, String vehicleId) {
        return Specification
                .where(hasStatus(status))
                .and(hasVehicle(vehicleId));
    }

    private static Specification<BookingRequest> hasStatus(BookingRequestStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<BookingRequest> hasVehicle(String vehicleId) {
        return (root, query, cb) -> {
            if (vehicleId == null || vehicleId.isBlank()) return null;
            Join<BookingRequest, Vehicle> vehicle = root.join("vehicle");
            return cb.equal(vehicle.get("id"), vehicleId);
        };
    }
}
