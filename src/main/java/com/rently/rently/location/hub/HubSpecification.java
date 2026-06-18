package com.rently.rently.location.hub;

import com.rently.rently.location.branch.Branch;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class HubSpecification {

    private HubSpecification() {}

    public static Specification<Hub> withFilters(String branchId, Boolean active, HubType type) {
        return Specification
                .where(hasBranch(branchId))
                .and(hasActive(active))
                .and(hasType(type));
    }

    private static Specification<Hub> hasBranch(String branchId) {
        return (root, query, cb) -> {
            if (branchId == null || branchId.isBlank()) return null;
            Join<Hub, Branch> branch = root.join("branch");
            return cb.equal(branch.get("id"), branchId);
        };
    }

    private static Specification<Hub> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;
            return cb.equal(root.get("isActive"), active);
        };
    }

    private static Specification<Hub> hasType(HubType type) {
        return (root, query, cb) -> {
            if (type == null) return null;
            return cb.equal(root.get("type"), type);
        };
    }
}
