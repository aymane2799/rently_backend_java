package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionRequest;
import com.rently.rently.billing.dto.SubscriptionResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImplementation implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionMapper mapper;
    private final SubscriptionInvoicePdfService invoicePdfService;

    @Override
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan with id " + request.getPlanId() + " not found"));

        Subscription subscription = Subscription.builder()
                .agencySlug(request.getAgencySlug())
                .plan(plan)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amountDue(request.getAmountDue())
                .build();

        return mapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public SubscriptionResponse markAsPaid(String id, String paymentMode) {
        Subscription subscription = findById(id);

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Subscription is already active");
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPaymentMode(paymentMode);
        subscription.setPaidAt(Instant.now());

        Subscription saved = subscriptionRepository.save(subscription);
        invoicePdfService.generateAsync(saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    public List<SubscriptionResponse> getAll() {
        return subscriptionRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public SubscriptionResponse getForAgency(String agencySlug) {
        Subscription subscription = subscriptionRepository.findCurrentByAgencySlug(agencySlug)
                .orElseThrow(() -> new EntityNotFoundException("No subscription found for agency " + agencySlug));
        return mapper.toResponse(subscription);
    }

    private Subscription findById(String id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription with id " + id + " not found"));
    }
}
