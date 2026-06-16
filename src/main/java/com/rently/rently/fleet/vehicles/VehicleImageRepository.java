package com.rently.rently.fleet.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, String> {
    List<VehicleImage> findByVehicleIdOrderByDisplayOrder(String vehicleId);
    Optional<VehicleImage> findFirstByVehicleIdAndIsPrimaryTrue(String vehicleId);
}
