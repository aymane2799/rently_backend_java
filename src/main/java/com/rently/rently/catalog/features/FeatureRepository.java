package com.rently.rently.catalog.features;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String>, JpaSpecificationExecutor<Feature> {
    Optional<Feature> findByName(String name);
    List<Feature> findAllByIsActive(boolean isActive);
}