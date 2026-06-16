package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyPublicProfileResponse;
import com.rently.rently.agency.dto.PublicVehicleResponse;
import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.fleet.vehicles.Transmission;
import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.fleet.vehicles.VehicleImage;
import com.rently.rently.fleet.vehicles.VehicleImageRepository;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.fleet.vehicles.VehicleStatus;
import com.rently.rently.multitenancy.TenantContext;
import com.rently.rently.reservation.reservation.ReservationRepository;
import com.rently.rently.reservation.reservation.ReservationStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublicAgencyServiceImplementation implements PublicAgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;
    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository vehicleImageRepository;
    private final ReservationRepository reservationRepository;
    private final PublicVehicleMapper publicVehicleMapper;

    @Override
    public AgencyPublicProfileResponse getPublicProfile(String slug) {
        return agencyMapper.toPublicProfileResponse(findActiveAgency(slug));
    }

    @Override
    public List<PublicVehicleResponse> getAvailableVehicles(String slug, LocalDateTime from, LocalDateTime to,
                                                            VehicleCategory category, Transmission transmission) {
        findActiveAgency(slug);

        TenantContext.setTenantId(slug);
        try {
            List<Vehicle> vehicles = transmission != null
                    ? vehicleRepository.findByStatusAndTransmission(VehicleStatus.AVAILABLE, transmission)
                    : vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);

            Set<String> occupiedVehicleIds = new HashSet<>();
            if (from != null && to != null) {
                occupiedVehicleIds.addAll(reservationRepository.findVehicleIdsWithOverlappingReservations(
                        ReservationStatus.ACTIVE, from, to));
            }

            final Set<String> finalOccupied = occupiedVehicleIds;
            return vehicles.stream()
                    .filter(v -> !finalOccupied.contains(v.getId()))
                    .map(v -> {
                        String primaryImageUrl = vehicleImageRepository
                                .findFirstByVehicleIdAndIsPrimaryTrue(v.getId())
                                .map(VehicleImage::getImageUrl)
                                .orElse(null);
                        return publicVehicleMapper.toResponse(v, primaryImageUrl);
                    })
                    .filter(r -> category == null || r.getCategory() == category)
                    .toList();
        } finally {
            TenantContext.clear();
        }
    }

    private Agency findActiveAgency(String slug) {
        Agency agency = agencyRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + slug));
        if (agency.getStatus() == AgencyStatus.BLOCKED) {
            throw new IllegalStateException("Agency is not available");
        }
        return agency;
    }
}
