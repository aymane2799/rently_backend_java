package com.rently.rently.billing;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan extends Auditable {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_monthly", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    @Column(name = "price_yearly", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceYearly;

    @Column(name = "max_branches")
    private Integer maxBranches;

    @Column(name = "max_hubs")
    private Integer maxHubs;

    @Column(name = "max_vehicles")
    private Integer maxVehicles;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
