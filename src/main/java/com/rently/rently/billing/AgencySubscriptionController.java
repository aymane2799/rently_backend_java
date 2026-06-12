package com.rently.rently.billing;

import com.rently.rently.auth.User;
import com.rently.rently.billing.dto.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings/subscription")
@RequiredArgsConstructor
public class AgencySubscriptionController {

    private final SubscriptionService service;

    @GetMapping
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public SubscriptionResponse get(@AuthenticationPrincipal User currentUser) {
        return service.getForAgency(currentUser.getAgencySlug());
    }
}
