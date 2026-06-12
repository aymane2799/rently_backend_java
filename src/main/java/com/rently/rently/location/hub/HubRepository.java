package com.rently.rently.location.hub;

import com.rently.rently.location.branch.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HubRepository extends JpaRepository<Hub, String> {
    List<Hub> findAllByBranch(Branch branch);
    List<Hub> findAllByBranchAndIsActive(Branch branch, boolean isActive);
    long countByIsActive(boolean isActive);
}
