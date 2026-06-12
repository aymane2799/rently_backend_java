package com.rently.rently.reservation.reservation;

import com.rently.rently.auth.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<ReservationResponse> getAll() {
        return service.getAll();
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
