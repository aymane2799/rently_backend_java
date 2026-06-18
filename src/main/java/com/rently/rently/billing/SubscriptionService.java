package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionRequest;
import com.rently.rently.billing.dto.SubscriptionResponse;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionService {
    SubscriptionResponse create(CreateSubscriptionRequest request);
    SubscriptionResponse markAsPaid(String id, String paymentMode);
    List<SubscriptionResponse> getAll();
    PagedResponse<SubscriptionResponse> getAll(SubscriptionStatus status, String agencySlug, String planId, LocalDate startDateFrom, LocalDate startDateTo, Pageable pageable);
    SubscriptionResponse getForAgency(String agencySlug);
}
