package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionRequest;
import com.rently.rently.billing.dto.MarkPaidRequest;
import com.rently.rently.billing.dto.SubscriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<SubscriptionResponse> getAll() {
        return service.getAll();
    }

    @PostMapping("/{id}/mark-paid")
    public SubscriptionResponse markAsPaid(@PathVariable String id,
                                           @RequestBody @Valid MarkPaidRequest request) {
        return service.markAsPaid(id, request.getPaymentMode());
    }
}
