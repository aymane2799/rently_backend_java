package com.rently.rently.catalog.repositories;

import com.rently.rently.catalog.entities.CarBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarBrandRepository extends JpaRepository<CarBrand, String> {
    Optional<CarBrand> findByName(String name);
}