package com.rently.rently.reservation.booking;

import com.rently.rently.auth.User;
import com.rently.rently.reservation.booking.dto.BookingRequestResponse;
import com.rently.rently.reservation.booking.dto.RejectBookingRequestRequest;
import com.rently.rently.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking-requests")
@RequiredArgsConstructor
public class BookingRequestController {

    private final BookingRequestService bookingRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'BRANCH_MANAGER', 'AGENCY_OWNER')")
    public PagedResponse<BookingRequestResponse> getAll(
            @RequestParam(required = false) BookingRequestStatus status,
            @RequestParam(required = false) String vehicleId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return bookingRequestService.getAll(status, vehicleId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'BRANCH_MANAGER', 'AGENCY_OWNER')")
    public BookingRequestResponse get(@PathVariable String id) {
        return bookingRequestService.get(id);
    }

    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('AGENT', 'BRANCH_MANAGER', 'AGENCY_OWNER')")
    public BookingRequestResponse confirm(
            @PathVariable String id,
            @AuthenticationPrincipal User agent) {
        return bookingRequestService.confirm(id, agent.getId());
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENT', 'BRANCH_MANAGER', 'AGENCY_OWNER')")
    public void reject(
            @PathVariable String id,
            @Valid @RequestBody RejectBookingRequestRequest request,
            @AuthenticationPrincipal User agent) {
        bookingRequestService.reject(id, agent.getId(), request.getRejectionReason());
    }
}
