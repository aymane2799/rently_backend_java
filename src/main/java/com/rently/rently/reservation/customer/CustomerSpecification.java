package com.rently.rently.reservation.customer;

import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    private CustomerSpecification() {}

    public static Specification<Customer> withFilters(IdType idType, String search) {
        return Specification
                .where(hasIdType(idType))
                .and(hasSearch(search));
    }

    private static Specification<Customer> hasIdType(IdType idType) {
        return (root, query, cb) -> {
            if (idType == null) return null;
            return cb.equal(root.get("idType"), idType);
        };
    }

    private static Specification<Customer> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("idNumber")), pattern)
            );
        };
    }
}
