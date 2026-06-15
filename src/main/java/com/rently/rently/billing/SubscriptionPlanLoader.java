package com.rently.rently.billing;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SubscriptionPlanLoader {

    private final SubscriptionPlanRepository planRepository;

    @Cacheable(value = "subscriptionPlans", key = "#planId")
    public SubscriptionPlan load(String planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("SubscriptionPlan not found: " + planId));
    }
}
