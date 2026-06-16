package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyPublicProfileResponse;
import com.rently.rently.agency.dto.PublicVehicleResponse;
import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.fleet.vehicles.Transmission;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicAgencyController {

    private final PublicAgencyService publicAgencyService;

    @GetMapping("/{slug}")
    public AgencyPublicProfileResponse getPublicProfile(@PathVariable String slug) {
        return publicAgencyService.getPublicProfile(slug);
    }

    @GetMapping("/{slug}/vehicles")
    public List<PublicVehicleResponse> getAvailableVehicles(
            @PathVariable String slug,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) VehicleCategory category,
            @RequestParam(required = false) Transmission transmission) {
        return publicAgencyService.getAvailableVehicles(slug, from, to, category, transmission);
    }
}
