package com.rently.rently.billing;

import com.rently.rently.billing.dto.SubscriptionPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService service;

    @GetMapping
    public List<SubscriptionPlanResponse> getActivePlans() {
        return service.getActivePlans();
    }
}
