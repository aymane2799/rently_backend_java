package com.rently.rently.billing.dto;

import com.rently.rently.billing.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class SubscriptionResponse {
    private String id;
    private String agencySlug;
    private SubscriptionPlanResponse plan;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amountDue;
    private String paymentMode;
    private Instant paidAt;
    private String invoiceUrl;
}
