package com.rently.rently.auth;

import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> withFilters(String agencySlug, UserRole role, String branchId, Boolean active, String search) {
        return Specification
                .where(hasAgencySlug(agencySlug))
                .and(hasRole(role))
                .and(hasBranch(branchId))
                .and(hasActive(active))
                .and(hasSearch(search));
    }

    private static Specification<User> hasAgencySlug(String agencySlug) {
        return (root, query, cb) -> {
            if (agencySlug == null || agencySlug.isBlank()) return null;
            return cb.equal(root.get("agencySlug"), agencySlug);
        };
    }

    private static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> {
            if (role == null) return null;
            return cb.equal(root.get("role"), role);
        };
    }

    private static Specification<User> hasBranch(String branchId) {
        return (root, query, cb) -> {
            if (branchId == null || branchId.isBlank()) return null;
            return cb.equal(root.get("branchId"), branchId);
        };
    }

    private static Specification<User> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;
            return cb.equal(root.get("active"), active);
        };
    }

    private static Specification<User> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }
}
