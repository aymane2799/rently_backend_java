package com.rently.rently.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    List<Subscription> findAllByAgencySlug(String agencySlug);

    @Query("SELECT s FROM Subscription s WHERE s.agencySlug = :slug ORDER BY s.createdAt DESC LIMIT 1")
    Optional<Subscription> findCurrentByAgencySlug(@Param("slug") String agencySlug);
}
