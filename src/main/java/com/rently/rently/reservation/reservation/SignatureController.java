package com.rently.rently.reservation.reservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class SignatureController {

    private final ReservationService service;

    @PostMapping("/{id}/signature")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AGENT')")
    public void sign(
            @PathVariable String id,
            @RequestBody @Valid SignatureRequest request
    ) {
        service.sign(id, request.getSignatureBase64());
    }

    @PostMapping("/{id}/mark-printed")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AGENT')")
    public void markPrinted(@PathVariable String id) {
        service.markPrinted(id);
    }
}
