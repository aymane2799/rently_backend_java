package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionRequest;
import com.rently.rently.billing.dto.MarkPaidRequest;
import com.rently.rently.billing.dto.SubscriptionResponse;
import com.rently.rently.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@RequestBody @Valid CreateSubscriptionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PagedResponse<SubscriptionResponse> getAll(
            @RequestParam(required = false) SubscriptionStatus status,
            @RequestParam(required = false) String agencySlug,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return service.getAll(status, agencySlug, planId, startDateFrom, startDateTo, pageable);
    }

    @PostMapping("/{id}/mark-paid")
    public SubscriptionResponse markAsPaid(@PathVariable String id,
                                           @RequestBody @Valid MarkPaidRequest request) {
        return service.markAsPaid(id, request.getPaymentMode());
    }
}
