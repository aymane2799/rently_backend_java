package com.rently.rently.location.branch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String> {
    Optional<Branch> findByName(String name);
    List<Branch> findAllByIsActive(boolean isActive);
    long countByIsActive(boolean isActive);
}
