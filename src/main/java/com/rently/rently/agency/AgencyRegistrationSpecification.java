package com.rently.rently.agency;

import org.springframework.data.jpa.domain.Specification;

public class AgencyRegistrationSpecification {

    private AgencyRegistrationSpecification() {}

    public static Specification<AgencyRegistration> withFilters(AgencyRegistrationStatus status, String city, String search) {
        return Specification
                .where(hasStatus(status))
                .and(hasCity(city))
                .and(hasSearch(search));
    }

    private static Specification<AgencyRegistration> hasStatus(AgencyRegistrationStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    private static Specification<AgencyRegistration> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) return null;
            return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    private static Specification<AgencyRegistration> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("agencyName")), pattern),
                    cb.like(cb.lower(root.get("ownerEmail")), pattern),
                    cb.like(cb.lower(root.get("ownerFirstName")), pattern),
                    cb.like(cb.lower(root.get("ownerLastName")), pattern)
            );
        };
    }
}
