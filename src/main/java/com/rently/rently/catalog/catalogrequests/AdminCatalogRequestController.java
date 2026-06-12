package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.auth.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/catalog-requests")
@RequiredArgsConstructor
public class AdminCatalogRequestController {

    private final CatalogRequestService service;

    @GetMapping
    public List<CatalogRequestResponse> getAll(
            @RequestParam(required = false) CatalogRequestType type,
            @RequestParam(required = false) CatalogRequestStatus status) {
        return service.getAll(type, status);
    }

    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable String id, @AuthenticationPrincipal User currentUser) {
        service.approve(id, currentUser.getId());
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(
            @PathVariable String id,
            @RequestBody @Valid RejectCatalogRequestRequest request,
            @AuthenticationPrincipal User currentUser) {
        service.reject(id, request.getRejectionReason(), currentUser.getId());
    }
}
