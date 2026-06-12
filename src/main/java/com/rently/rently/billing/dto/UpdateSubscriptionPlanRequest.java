package com.rently.rently.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSubscriptionPlanRequest {

    @Size(max = 100)
    private String displayName;

    private String description;

    @DecimalMin("0.00")
    private BigDecimal priceMonthly;

    @DecimalMin("0.00")
    private BigDecimal priceYearly;

    private Integer maxBranches;
    private Integer maxHubs;
    private Integer maxVehicles;
}
