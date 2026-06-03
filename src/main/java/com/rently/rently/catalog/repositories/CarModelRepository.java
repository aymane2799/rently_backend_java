package com.rently.rently.catalog.repositories;

import com.rently.rently.catalog.entities.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel, String> {
    Optional<CarModel> findByName(String name);
}