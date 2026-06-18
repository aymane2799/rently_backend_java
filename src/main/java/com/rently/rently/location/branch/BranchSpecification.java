package com.rently.rently.location.branch;

import org.springframework.data.jpa.domain.Specification;

public class BranchSpecification {

    private BranchSpecification() {}

    public static Specification<Branch> withFilters(Boolean active, String city, String search) {
        return Specification
                .where(hasActive(active))
                .and(hasCity(city))
                .and(hasSearch(search));
    }

    private static Specification<Branch> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;
            return cb.equal(root.get("isActive"), active);
        };
    }

    private static Specification<Branch> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) return null;
            return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    private static Specification<Branch> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }
}
