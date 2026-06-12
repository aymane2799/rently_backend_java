package com.rently.rently.billing;

import com.rently.rently.auth.User;
import com.rently.rently.billing.dto.CreateSubscriptionRequest;
import com.rently.rently.billing.dto.MarkPaidRequest;
import com.rently.rently.billing.dto.SubscriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @PostMapping("/api/v1/admin/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@RequestBody @Valid CreateSubscriptionRequest request) {
        return service.create(request);
    }

    @GetMapping("/api/v1/admin/subscriptions")
    public List<SubscriptionResponse> getAll() {
        return service.getAll();
    }

    @PostMapping("/api/v1/admin/subscriptions/{id}/mark-paid")
    public SubscriptionResponse markAsPaid(@PathVariable String id,
                                           @RequestBody @Valid MarkPaidRequest request) {
        return service.markAsPaid(id, request.getPaymentMode());
    }

    @GetMapping("/api/v1/settings/subscription")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public SubscriptionResponse getForAgency(@AuthenticationPrincipal User currentUser) {
        return service.getForAgency(currentUser.getAgencySlug());
    }
}
