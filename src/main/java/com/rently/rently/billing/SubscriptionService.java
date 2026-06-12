package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionRequest;
import com.rently.rently.billing.dto.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse create(CreateSubscriptionRequest request);
    SubscriptionResponse markAsPaid(String id, String paymentMode);
    List<SubscriptionResponse> getAll();
    SubscriptionResponse getForAgency(String agencySlug);
}
