package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionPlanRequest;
import com.rently.rently.billing.dto.SubscriptionPlanResponse;
import com.rently.rently.billing.dto.UpdateSubscriptionPlanRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/plans")
@RequiredArgsConstructor
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanService service;

    @GetMapping
    public List<SubscriptionPlanResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionPlanResponse create(@RequestBody @Valid CreateSubscriptionPlanRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id,
                       @RequestBody @Valid UpdateSubscriptionPlanRequest request) {
        service.update(id, request);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String id) {
        service.deactivate(id);
    }
}
