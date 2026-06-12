package com.rently.rently.billing;

import com.rently.rently.billing.dto.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper {

    private final SubscriptionPlanMapper planMapper;

    public SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .agencySlug(subscription.getAgencySlug())
                .plan(planMapper.toResponse(subscription.getPlan()))
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .amountDue(subscription.getAmountDue())
                .paymentMode(subscription.getPaymentMode())
                .paidAt(subscription.getPaidAt())
                .invoiceUrl(subscription.getInvoiceUrl())
                .build();
    }
}
