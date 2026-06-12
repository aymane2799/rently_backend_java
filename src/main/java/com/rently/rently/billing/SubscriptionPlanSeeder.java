package com.rently.rently.billing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanSeeder implements ApplicationRunner {

    private final SubscriptionPlanRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        seedIfAbsent("SAFI", "Safi", "Starter plan for small agencies",
                new BigDecimal("499.00"), new BigDecimal("4990.00"),
                1, 1, 15);

        seedIfAbsent("CHAMIL", "Chamil", "Unlimited plan for large agencies",
                new BigDecimal("1499.00"), new BigDecimal("14990.00"),
                null, null, null);
    }

    private void seedIfAbsent(String code, String displayName, String description,
                               BigDecimal priceMonthly, BigDecimal priceYearly,
                               Integer maxBranches, Integer maxHubs, Integer maxVehicles) {
        if (!repository.existsByCode(code)) {
            repository.save(SubscriptionPlan.builder()
                    .code(code)
                    .displayName(displayName)
                    .description(description)
                    .priceMonthly(priceMonthly)
                    .priceYearly(priceYearly)
                    .maxBranches(maxBranches)
                    .maxHubs(maxHubs)
                    .maxVehicles(maxVehicles)
                    .build());
            log.info("Seeded subscription plan: {}", code);
        }
    }
}
