package com.rently.rently.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AgencyRegistrationRepository extends JpaRepository<AgencyRegistration, String>, JpaSpecificationExecutor<AgencyRegistration> {
    List<AgencyRegistration> findAllByStatus(AgencyRegistrationStatus status);
    boolean existsByRcNumber(String rcNumber);
    boolean existsByIceNumber(String iceNumber);
    boolean existsByOwnerEmail(String ownerEmail);
}
