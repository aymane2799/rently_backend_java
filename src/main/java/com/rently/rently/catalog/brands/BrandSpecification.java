package com.rently.rently.catalog.brands;

import org.springframework.data.jpa.domain.Specification;

public class BrandSpecification {

    private BrandSpecification() {}

    public static Specification<Brand> withFilters(String search, Boolean isActive) {
        return Specification
                .where(hasSearch(search))
                .and(hasIsActive(isActive));
    }

    private static Specification<Brand> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    private static Specification<Brand> hasIsActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
