package com.rently.rently.fleet.vehicles;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByInsuranceNumber(String insuranceNumber);

    @EntityGraph(value = "Vehicle.details")
    List<Vehicle> findAll();

    @EntityGraph(value = "Vehicle.details")
    Optional<Vehicle> findById(String id);
}
