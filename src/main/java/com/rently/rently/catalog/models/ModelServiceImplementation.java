package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.catalog.models.hydration.ModelHydrationContext;
import com.rently.rently.catalog.models.hydration.ModelHydrator;
import com.rently.rently.shared.PagedResponse;
import com.rently.rently.shared.PagedResponseMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelServiceImplementation implements ModelService {
    private final ModelRepository repository;
    private final ModelMapper modelMapper;
    private final ModelHydrator hydrator;

    @Override
    public List<ModelResponse> getAll() {
        return repository.findAllByIsActive(true)
                .stream()
                .map(modelMapper::toResponse)
                .toList();
    }

    @Override
    public PagedResponse<ModelResponse> getAll(String search, String brandId, VehicleCategory category, Boolean isActive, Pageable pageable) {
        Specification<Model> spec = ModelSpecification.withFilters(search, brandId, category, isActive);
        return PagedResponseMapper.toPagedResponse(repository.findAll(spec, pageable), modelMapper::toResponse);
    }

    @Override
    public List<ModelOptionResponse> getOptions(String brandId) {
        Specification<Model> spec = ModelSpecification.withFilters(null, brandId, null, true);
        return repository.findAll(spec)
                .stream()
                .map(m -> new ModelOptionResponse(m.getId(), m.getName(), m.getBrand().getId(), m.getBrand().getName(), m.getCategory()))
                .toList();
    }

    @Override
    public ModelResponse get(String id) {
        final Model model = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car Model with id " + id + " not found!"));
        return modelMapper.toResponse(model);
    }

    @Override
    public ModelResponse create(CreateModelRequest request) {
        if (repository.findByName(request.getName()).isPresent()) {
            throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
        }

        final ModelHydrationContext context = hydrator.hydrate(request);
        final Model entity = modelMapper.toEntity(request, context);
        return modelMapper.toResponse(repository.save(entity));
    }

    @Override
    public void update(String id, UpdateModelRequest request) {
        final Model model = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car Model with id " + id + " not found!"));

        if (request.getName() != null) {
            repository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(model.getId())) {
                    throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
                }
            });
        }

        final ModelHydrationContext context = hydrator.hydrate(request);
        modelMapper.patchEntity(model, request, context);
        repository.save(model);
    }

    @Override
    public void delete(String id) {
        final Model model = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car Model with id " + id + " not found!"));
        model.setActive(false);
        repository.save(model);
    }
}
