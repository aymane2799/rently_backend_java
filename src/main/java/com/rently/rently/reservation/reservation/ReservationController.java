package com.rently.rently.reservation.reservation;

import com.rently.rently.auth.User;
import com.rently.rently.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENT', 'BRANCH_MANAGER')")
    public ReservationResponse create(
            @RequestBody @Valid CreateReservationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return service.create(request, currentUser.getId());
    }

    @GetMapping
    public PagedResponse<ReservationResponse> getAll(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) ContractStatus contractStatus,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTo,
            @PageableDefault(size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.getAll(status, contractStatus, customerId, vehicleId, startDateFrom, startDateTo, pageable);
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'AGENCY_OWNER')")
    public void close(@PathVariable String id) {
        service.close(id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'AGENCY_OWNER')")
    public void cancel(@PathVariable String id) {
        service.cancel(id);
    }
}
