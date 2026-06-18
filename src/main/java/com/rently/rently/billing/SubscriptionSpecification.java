package com.rently.rently.billing;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SubscriptionSpecification {

    private SubscriptionSpecification() {}

    public static Specification<Subscription> withFilters(
            SubscriptionStatus status,
            String agencySlug,
            String planId,
            LocalDate startDateFrom,
            LocalDate startDateTo
    ) {
        return Specification
                .where(hasStatus(status))
                .and(hasAgencySlug(agencySlug))
                .and(hasPlan(planId))
                .and(startDateFrom(startDateFrom))
                .and(startDateTo(startDateTo));
    }

    private static Specification<Subscription> hasStatus(SubscriptionStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<Subscription> hasAgencySlug(String agencySlug) {
        return (root, query, cb) -> {
            if (agencySlug == null || agencySlug.isBlank()) return null;
            return cb.equal(root.get("agencySlug"), agencySlug);
        };
    }

    private static Specification<Subscription> hasPlan(String planId) {
        return (root, query, cb) -> {
            if (planId == null || planId.isBlank()) return null;
            Join<Subscription, SubscriptionPlan> plan = root.join("plan");
            return cb.equal(plan.get("id"), planId);
        };
    }

    private static Specification<Subscription> startDateFrom(LocalDate from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("startDate"), from);
        };
    }

    private static Specification<Subscription> startDateTo(LocalDate to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("startDate"), to);
        };
    }
}
