package com.rently.rently.location.branch;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImplementation implements BranchService {

    private final BranchRepository repository;
    private final BranchMapper mapper;

    @Override
    public List<BranchResponse> getAll() {
        return repository.findAllByIsActive(true).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public BranchResponse get(String id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " not found!"));
        return mapper.toResponse(branch);
    }

    @Override
    public BranchResponse create(CreateBranchRequest request) {
        // TODO: Phase 12 — QuotaService.assertCanAddBranch()
        if (repository.findByName(request.getName()).isPresent()) {
            throw new EntityExistsException("Branch with name '" + request.getName() + "' already exists!");
        }
        Branch branch = mapper.toEntity(request, null);
        return mapper.toResponse(repository.save(branch));
    }

    @Override
    public void update(String id, UpdateBranchRequest request) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " not found!"));

        if (request.getName() != null) {
            repository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(branch.getId())) {
                    throw new EntityExistsException("Branch with name '" + request.getName() + "' already exists!");
                }
            });
        }

        mapper.patchEntity(branch, request, null);
        repository.save(branch);
    }

    @Override
    public void delete(String id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch with id " + id + " not found!"));
        branch.setActive(false);
        repository.save(branch);
    }
}
