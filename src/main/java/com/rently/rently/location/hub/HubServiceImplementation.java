package com.rently.rently.location.hub;

import com.rently.rently.billing.QuotaService;
import com.rently.rently.location.branch.Branch;
import com.rently.rently.location.branch.BranchRepository;
import com.rently.rently.location.hub.hydration.HubHydrationContext;
import com.rently.rently.location.hub.hydration.HubHydrator;
import com.rently.rently.shared.PagedResponse;
import com.rently.rently.shared.PagedResponseMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HubServiceImplementation implements HubService {

    private final HubRepository repository;
    private final BranchRepository branchRepository;
    private final HubMapper mapper;
    private final HubHydrator hydrator;
    private final QuotaService quotaService;

    @Override
    public List<HubResponse> getAllByBranch(String branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + branchId + " not found!"));
        return repository.findAllByBranchAndIsActive(branch, true).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public PagedResponse<HubResponse> getAllByBranch(String branchId, Boolean active, HubType type, Pageable pageable) {
        Specification<Hub> spec = HubSpecification.withFilters(branchId, active, type);
        return PagedResponseMapper.toPagedResponse(repository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    public List<HubOptionResponse> getOptions(String branchId) {
        Specification<Hub> spec = HubSpecification.withFilters(branchId, true, null);
        return repository.findAll(spec).stream()
                .map(h -> new HubOptionResponse(h.getId(), h.getName(), h.getType(), h.getBranch().getId()))
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
        quotaService.assertCanAddHub();
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
