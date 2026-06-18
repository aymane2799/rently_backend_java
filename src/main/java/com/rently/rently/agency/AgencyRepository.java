package com.rently.rently.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, String>, JpaSpecificationExecutor<Agency> {
    Optional<Agency> findBySlug(String slug);
    Optional<Agency> findByEmail(String email);
    List<Agency> findAllByStatus(AgencyStatus status);
    boolean existsBySlug(String slug);
}
