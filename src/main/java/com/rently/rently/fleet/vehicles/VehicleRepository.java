package com.rently.rently.fleet.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByInsuranceNumber(String insuranceNumber);
}
