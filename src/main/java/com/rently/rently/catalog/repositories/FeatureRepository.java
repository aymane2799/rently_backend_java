package com.rently.rently.catalog.repositories;

import com.rently.rently.catalog.entities.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, UUID> {
}