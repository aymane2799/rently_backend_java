package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.RejectRegistrationRequest;
import com.rently.rently.auth.User;
import com.rently.rently.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/agencies/registrations")
@RequiredArgsConstructor
public class AdminRegistrationController {

    private final AgencyRegistrationService service;

    @GetMapping
    public PagedResponse<AgencyRegistrationResponse> getAll(
            @RequestParam(required = false) AgencyRegistrationStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.getAll(status, city, search, pageable);
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
