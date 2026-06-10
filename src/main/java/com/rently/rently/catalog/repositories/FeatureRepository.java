package com.rently.rently.catalog.repositories;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.entities.Feature;
import com.rently.rently.catalog.reponses.FeatureResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    Optional<Feature> findByName(String name);

}