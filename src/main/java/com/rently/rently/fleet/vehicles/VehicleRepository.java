package com.rently.rently.fleet.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    Optional<Vehicle> findByInsuranceNumber(String insuranceNumber);
    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByStatusAndTransmission(VehicleStatus status, Transmission transmission);

    long countByStatus(VehicleStatus status);
    long countByStatusAndCurrentHubIdIn(VehicleStatus status, Collection<String> hubIds);
    long countByCurrentHubIdIn(Collection<String> hubIds);
    List<Vehicle> findByCurrentHubIdIn(Collection<String> hubIds);

    List<Vehicle> findByInsuranceExpiresAtBetween(LocalDate from, LocalDate to);

    List<Vehicle> findByInsuranceExpiresAtBetweenAndCurrentHubIdIn(LocalDate from, LocalDate to, Collection<String> hubIds);
}
