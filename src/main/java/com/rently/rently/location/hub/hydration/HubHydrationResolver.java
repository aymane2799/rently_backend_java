package com.rently.rently.location.hub.hydration;

import com.rently.rently.location.branch.Branch;
import com.rently.rently.location.branch.BranchRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubHydrationResolver {

    private final BranchRepository branchRepository;

    public Branch resolveBranch(String id) {
        if (id == null) return null;
        return branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " not found!"));
    }
}
