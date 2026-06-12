package com.rently.rently.billing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    Optional<SubscriptionPlan> findByCode(String code);
    boolean existsByCode(String code);
    List<SubscriptionPlan> findAllByActiveTrue();
}
