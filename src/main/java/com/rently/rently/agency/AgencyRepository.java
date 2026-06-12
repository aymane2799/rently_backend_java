package com.rently.rently.agency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgencyRepository extends JpaRepository<Agency, String> {
    Optional<Agency> findBySlug(String slug);
    Optional<Agency> findByEmail(String email);
    List<Agency> findAllByStatus(AgencyStatus status);
    boolean existsBySlug(String slug);
}
