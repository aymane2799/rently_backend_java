package com.rently.rently.document;

import com.rently.rently.auth.User;
import com.rently.rently.billing.Subscription;
import com.rently.rently.billing.SubscriptionRepository;
import com.rently.rently.reservation.reservation.Reservation;
import com.rently.rently.reservation.reservation.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentStatusController {

    private final ReservationRepository reservationRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/reservations/{id}/contract/status")
    public DocumentStatusResponse contractStatus(@PathVariable String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
        return new DocumentStatusResponse(reservation.getContractUrl() != null, reservation.getContractUrl());
    }

    @GetMapping("/reservations/{id}/invoice/status")
    public DocumentStatusResponse invoiceStatus(@PathVariable String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + id));
        return new DocumentStatusResponse(reservation.getInvoiceUrl() != null, reservation.getInvoiceUrl());
    }

    @GetMapping("/settings/subscription/invoice/status")
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public DocumentStatusResponse subscriptionInvoiceStatus(@AuthenticationPrincipal User currentUser) {
        Subscription subscription = subscriptionRepository.findCurrentByAgencySlug(currentUser.getAgencySlug())
                .orElseThrow(() -> new EntityNotFoundException("No subscription found for agency: " + currentUser.getAgencySlug()));
        return new DocumentStatusResponse(subscription.getInvoiceUrl() != null, subscription.getInvoiceUrl());
    }
}
