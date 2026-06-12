package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.RejectRegistrationRequest;
import com.rently.rently.agency.dto.SubmitRegistrationRequest;
import com.rently.rently.auth.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AgencyRegistrationController {

    private final AgencyRegistrationService service;

    @PostMapping("/api/v1/agencies/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyRegistrationResponse submit(@RequestBody @Valid SubmitRegistrationRequest request) {
        return service.submit(request);
    }

    @GetMapping("/api/v1/admin/agencies/registrations")
    public List<AgencyRegistrationResponse> getAll(
            @RequestParam(required = false) AgencyRegistrationStatus status) {
        return service.getAll(status);
    }

    @GetMapping("/api/v1/admin/agencies/registrations/{id}")
    public AgencyRegistrationResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/api/v1/admin/agencies/registrations/{id}/approve")
    public AgencyRegistrationResponse approve(@PathVariable String id,
                                              @AuthenticationPrincipal User currentUser) {
        return service.approve(id, currentUser.getId());
    }

    @PostMapping("/api/v1/admin/agencies/registrations/{id}/reject")
    public AgencyRegistrationResponse reject(@PathVariable String id,
                                             @RequestBody @Valid RejectRegistrationRequest request,
                                             @AuthenticationPrincipal User currentUser) {
        return service.reject(id, request.getRejectionReason(), currentUser.getId());
    }
}
