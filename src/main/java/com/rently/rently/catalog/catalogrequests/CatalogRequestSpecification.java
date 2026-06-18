package com.rently.rently.catalog.catalogrequests;

import org.springframework.data.jpa.domain.Specification;

public class CatalogRequestSpecification {

    private CatalogRequestSpecification() {}

    public static Specification<CatalogRequest> withFilters(CatalogRequestType type, CatalogRequestStatus status, String agencySlug) {
        return Specification
                .where(hasType(type))
                .and(hasStatus(status))
                .and(hasAgencySlug(agencySlug));
    }

    private static Specification<CatalogRequest> hasType(CatalogRequestType type) {
        return (root, query, cb) -> {
            if (type == null) return null;
            return cb.equal(root.get("type"), type);
        };
    }

    private static Specification<CatalogRequest> hasStatus(CatalogRequestStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<CatalogRequest> hasAgencySlug(String agencySlug) {
        return (root, query, cb) -> {
            if (agencySlug == null || agencySlug.isBlank()) return null;
            return cb.equal(root.get("agencySlug"), agencySlug);
        };
    }
}
