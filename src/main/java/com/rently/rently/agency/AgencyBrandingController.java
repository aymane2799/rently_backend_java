package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyBrandingResponse;
import com.rently.rently.agency.dto.UpdateAgencyBrandingRequest;
import com.rently.rently.auth.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings/agency/branding")
@RequiredArgsConstructor
public class AgencyBrandingController {

    private final AgencyService agencyService;

    @GetMapping
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public AgencyBrandingResponse getBranding(@AuthenticationPrincipal User currentUser) {
        return agencyService.getBranding(currentUser.getAgencySlug());
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public void updateBranding(@RequestBody @Valid UpdateAgencyBrandingRequest request,
                               @AuthenticationPrincipal User currentUser) {
        agencyService.updateBranding(currentUser.getAgencySlug(), request);
    }
}
