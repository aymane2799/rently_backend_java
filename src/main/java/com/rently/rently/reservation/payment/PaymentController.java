package com.rently.rently.reservation.payment;

import com.rently.rently.auth.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations/{reservationId}/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @GetMapping
    public PaymentResponse get(@PathVariable String reservationId) {
        return service.getForReservation(reservationId);
    }

    @PostMapping("/release-deposit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'AGENCY_OWNER')")
    public void releaseDeposit(
            @PathVariable String reservationId,
            @AuthenticationPrincipal User currentUser
    ) {
        service.releaseDeposit(reservationId, currentUser.getId());
    }
}
