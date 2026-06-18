package com.rently.rently.catalog.models;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, String>, JpaSpecificationExecutor<Model> {
    Optional<Model> findByName(String name);

    @EntityGraph(value = "Model.brand")
    List<Model> findAll();

    @EntityGraph(value = "Model.brand")
    List<Model> findAllByIsActive(boolean isActive);
}