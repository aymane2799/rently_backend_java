package com.rently.rently.agency;

import org.springframework.data.jpa.domain.Specification;

public class AgencySpecification {

    private AgencySpecification() {}

    public static Specification<Agency> withFilters(AgencyStatus status, String city, String planId, String search) {
        return Specification
                .where(hasStatus(status))
                .and(hasCity(city))
                .and(hasPlan(planId))
                .and(hasSearch(search));
    }

    private static Specification<Agency> hasStatus(AgencyStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<Agency> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) return null;
            return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    private static Specification<Agency> hasPlan(String planId) {
        return (root, query, cb) -> {
            if (planId == null || planId.isBlank()) return null;
            return cb.equal(root.get("planId"), planId);
        };
    }

    private static Specification<Agency> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern)
            );
        };
    }
}
