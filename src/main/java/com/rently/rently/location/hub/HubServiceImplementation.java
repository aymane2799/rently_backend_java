package com.rently.rently.location.hub;

import com.rently.rently.location.branch.Branch;
import com.rently.rently.location.branch.BranchRepository;
import com.rently.rently.location.hub.hydration.HubHydrationContext;
import com.rently.rently.location.hub.hydration.HubHydrator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HubServiceImplementation implements HubService {

    private final HubRepository repository;
    private final BranchRepository branchRepository;
    private final HubMapper mapper;
    private final HubHydrator hydrator;

    @Override
    public List<HubResponse> getAllByBranch(String branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + branchId + " not found!"));
        return repository.findAllByBranchAndIsActive(branch, true).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public HubResponse get(String id) {
        Hub hub = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hub with id " + id + " not found!"));
        return mapper.toResponse(hub);
    }

    @Override
    public HubResponse create(String branchId, CreateHubRequest request) {
        // TODO: Phase 12 — QuotaService.assertCanAddHub()
        HubHydrationContext ctx = hydrator.hydrate(branchId);
        Hub hub = mapper.toEntity(request, ctx);
        return mapper.toResponse(repository.save(hub));
    }

    @Override
    public void update(String id, UpdateHubRequest request) {
        Hub hub = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hub with id " + id + " not found!"));
        mapper.patchEntity(hub, request, hydrator.emptyContext());
        repository.save(hub);
    }

    @Override
    public void deactivate(String id) {
        Hub hub = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hub with id " + id + " not found!"));
        hub.setActive(false);
        repository.save(hub);
    }
}
