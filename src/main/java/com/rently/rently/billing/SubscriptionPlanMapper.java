package com.rently.rently.billing;

import com.rently.rently.billing.dto.SubscriptionPlanResponse;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .displayName(plan.getDisplayName())
                .description(plan.getDescription())
                .priceMonthly(plan.getPriceMonthly())
                .priceYearly(plan.getPriceYearly())
                .maxBranches(plan.getMaxBranches())
                .maxHubs(plan.getMaxHubs())
                .maxVehicles(plan.getMaxVehicles())
                .active(plan.isActive())
                .build();
    }
}
