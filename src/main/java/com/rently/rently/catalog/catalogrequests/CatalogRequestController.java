package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.auth.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog-requests")
@RequiredArgsConstructor
public class CatalogRequestController {

    private final CatalogRequestService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public CatalogRequestResponse submit(
            @RequestBody @Valid SubmitCatalogRequestRequest request,
            @AuthenticationPrincipal User currentUser) {
        return service.submit(request, currentUser.getAgencySlug());
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public List<CatalogRequestResponse> getForAgency(@AuthenticationPrincipal User currentUser) {
        return service.getForAgency(currentUser.getAgencySlug());
    }
}
