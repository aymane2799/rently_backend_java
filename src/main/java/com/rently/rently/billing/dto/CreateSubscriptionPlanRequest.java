package com.rently.rently.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSubscriptionPlanRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String displayName;

    private String description;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal priceMonthly;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal priceYearly;

    private Integer maxBranches;
    private Integer maxHubs;
    private Integer maxVehicles;
}
