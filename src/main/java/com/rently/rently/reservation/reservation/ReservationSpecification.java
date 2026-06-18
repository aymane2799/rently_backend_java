package com.rently.rently.reservation.reservation;

import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.reservation.customer.Customer;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ReservationSpecification {

    private ReservationSpecification() {}

    public static Specification<Reservation> withFilters(
            ReservationStatus status,
            ContractStatus contractStatus,
            String customerId,
            String vehicleId,
            LocalDateTime startDateFrom,
            LocalDateTime startDateTo
    ) {
        return Specification
                .where(hasStatus(status))
                .and(hasContractStatus(contractStatus))
                .and(hasCustomer(customerId))
                .and(hasVehicle(vehicleId))
                .and(startDateFrom(startDateFrom))
                .and(startDateTo(startDateTo));
    }

    private static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<Reservation> hasContractStatus(ContractStatus contractStatus) {
        return (root, query, cb) -> {
            if (contractStatus == null) return null;
            return cb.equal(root.get("contractStatus"), contractStatus);
        };
    }

    private static Specification<Reservation> hasCustomer(String customerId) {
        return (root, query, cb) -> {
            if (customerId == null || customerId.isBlank()) return null;
            Join<Reservation, Customer> customer = root.join("customer");
            return cb.equal(customer.get("id"), customerId);
        };
    }

    private static Specification<Reservation> hasVehicle(String vehicleId) {
        return (root, query, cb) -> {
            if (vehicleId == null || vehicleId.isBlank()) return null;
            Join<Reservation, Vehicle> vehicle = root.join("vehicle");
            return cb.equal(vehicle.get("id"), vehicleId);
        };
    }

    private static Specification<Reservation> startDateFrom(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("startDate"), from);
        };
    }

    private static Specification<Reservation> startDateTo(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("startDate"), to);
        };
    }
}
