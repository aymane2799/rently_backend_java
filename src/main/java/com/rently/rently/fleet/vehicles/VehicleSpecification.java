package com.rently.rently.fleet.vehicles;

import org.springframework.data.jpa.domain.Specification;

public class VehicleSpecification {

    private VehicleSpecification() {}

    public static Specification<Vehicle> withFilters(VehicleStatus status, Transmission transmission, FuelType fuelType, String currentHubId, String search) {
        return Specification
                .where(hasStatus(status))
                .and(hasTransmission(transmission))
                .and(hasFuelType(fuelType))
                .and(hasCurrentHub(currentHubId))
                .and(hasSearch(search));
    }

    private static Specification<Vehicle> hasStatus(VehicleStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<Vehicle> hasTransmission(Transmission transmission) {
        return (root, query, cb) -> {
            if (transmission == null) return null;
            return cb.equal(root.get("transmission"), transmission);
        };
    }

    private static Specification<Vehicle> hasFuelType(FuelType fuelType) {
        return (root, query, cb) -> {
            if (fuelType == null) return null;
            return cb.equal(root.get("fuelType"), fuelType);
        };
    }

    private static Specification<Vehicle> hasCurrentHub(String currentHubId) {
        return (root, query, cb) -> {
            if (currentHubId == null || currentHubId.isBlank()) return null;
            return cb.equal(root.get("currentHubId"), currentHubId);
        };
    }

    private static Specification<Vehicle> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("licensePlate")), "%" + search.toLowerCase() + "%");
        };
    }
}
