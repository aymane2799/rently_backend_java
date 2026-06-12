package com.rently.rently.billing.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SubscriptionPlanResponse {
    private String id;
    private String code;
    private String displayName;
    private String description;
    private BigDecimal priceMonthly;
    private BigDecimal priceYearly;
    private Integer maxBranches;
    private Integer maxHubs;
    private Integer maxVehicles;
    private boolean active;
}
