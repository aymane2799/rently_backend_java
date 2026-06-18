package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.catalog.brands.Brand;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ModelSpecification {

    private ModelSpecification() {}

    public static Specification<Model> withFilters(String search, String brandId, VehicleCategory category, Boolean isActive) {
        return Specification
                .where(hasSearch(search))
                .and(hasBrand(brandId))
                .and(hasCategory(category))
                .and(hasIsActive(isActive));
    }

    private static Specification<Model> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    private static Specification<Model> hasBrand(String brandId) {
        return (root, query, cb) -> {
            if (brandId == null || brandId.isBlank()) return null;
            Join<Model, Brand> brand = root.join("brand");
            return cb.equal(brand.get("id"), brandId);
        };
    }

    private static Specification<Model> hasCategory(VehicleCategory category) {
        return (root, query, cb) -> {
            if (category == null) return null;
            return cb.equal(root.get("category"), category);
        };
    }

    private static Specification<Model> hasIsActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) return null;
            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
