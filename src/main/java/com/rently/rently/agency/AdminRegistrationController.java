package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.RejectRegistrationRequest;
import com.rently.rently.auth.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/agencies/registrations")
@RequiredArgsConstructor
public class AdminRegistrationController {

    private final AgencyRegistrationService service;

    @GetMapping
    public List<AgencyRegistrationResponse> getAll(
            @RequestParam(required = false) AgencyRegistrationStatus status) {
        return service.getAll(status);
    }

    @GetMapping("/{id}")
    public AgencyRegistrationResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/approve")
    public AgencyRegistrationResponse approve(@PathVariable String id,
                                              @AuthenticationPrincipal User currentUser) {
        return service.approve(id, currentUser.getId());
    }

    @PostMapping("/{id}/reject")
    public AgencyRegistrationResponse reject(@PathVariable String id,
                                             @RequestBody @Valid RejectRegistrationRequest request,
                                             @AuthenticationPrincipal User currentUser) {
        return service.reject(id, request.getRejectionReason(), currentUser.getId());
    }
}
