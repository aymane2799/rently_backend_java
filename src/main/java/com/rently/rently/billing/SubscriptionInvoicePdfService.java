package com.rently.rently.billing;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInvoicePdfService {

    private final SubscriptionRepository subscriptionRepository;

    @Async("documentExecutor")
    public void generateAsync(String subscriptionId) {
        log.info("Starting async invoice PDF generation for subscription {}", subscriptionId);
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new EntityNotFoundException("Subscription not found: " + subscriptionId));

            // Phase 11 will implement actual PDF generation.
            // Placeholder: log and leave invoiceUrl null until the document module is built.
            log.info("Invoice PDF generation placeholder complete for subscription {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for subscription {}: {}", subscriptionId, e.getMessage());
        }
    }
}
