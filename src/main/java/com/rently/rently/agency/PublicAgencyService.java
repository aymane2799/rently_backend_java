package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyPublicProfileResponse;
import com.rently.rently.agency.dto.PublicVehicleResponse;
import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.fleet.vehicles.Transmission;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicAgencyService {
    AgencyPublicProfileResponse getPublicProfile(String slug);
    List<PublicVehicleResponse> getAvailableVehicles(String slug, LocalDateTime from, LocalDateTime to,
                                                     VehicleCategory category, Transmission transmission);
}
