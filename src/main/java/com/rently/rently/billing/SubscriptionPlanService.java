package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionPlanRequest;
import com.rently.rently.billing.dto.SubscriptionPlanResponse;
import com.rently.rently.billing.dto.UpdateSubscriptionPlanRequest;

import java.util.List;

public interface SubscriptionPlanService {
    List<SubscriptionPlanResponse> getActivePlans();
    List<SubscriptionPlanResponse> getAll();
    SubscriptionPlanResponse get(String id);
    SubscriptionPlanResponse create(CreateSubscriptionPlanRequest request);
    SubscriptionPlanResponse update(String id, UpdateSubscriptionPlanRequest request);
    void deactivate(String id);
}
