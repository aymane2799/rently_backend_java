package com.rently.rently.catalog.features;

import org.springframework.data.jpa.domain.Specification;

public class FeatureSpecification {

    private FeatureSpecification() {}

    public static Specification<Feature> withFilters(String search, Boolean isActive) {
        return Specification
                .where(hasSearch(search))
                .and(hasIsActive(isActive));
    }

    private static Specification<Feature> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    private static Specification<Feature> hasIsActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
