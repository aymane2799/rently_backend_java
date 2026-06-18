package com.rently.rently.fleet.vehicles;

import com.rently.rently.shared.CRUDService;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface VehicleService extends CRUDService<CreateVehicleRequest, UpdateVehicleRequest, VehicleResponse, String> {
    PagedResponse<VehicleResponse> getAll(VehicleStatus status, Transmission transmission, FuelType fuelType, String currentHubId, String search, Pageable pageable);
}
