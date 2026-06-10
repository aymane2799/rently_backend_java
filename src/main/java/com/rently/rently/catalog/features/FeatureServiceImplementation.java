package com.rently.rently.catalog.features;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureServiceImplementation implements FeatureService {
    private final FeatureRepository repository;
    private final FeatureMapper featureMapper;

    @Override
    public List<FeatureResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(featureMapper::toResponse)
                .toList();
    }

    @Override
    public FeatureResponse get(String id) {
        final Feature feature = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature with id " + id + " not found!"));
        return featureMapper.toResponse(feature);
    }

    @Override
    public FeatureResponse create(CreateFeatureRequest request) {
        if (repository.findByName(request.getName()).isPresent()) {
            throw new EntityExistsException("Feature with name " + request.getName() + " already exists!");
        }

        final Feature entity = featureMapper.toEntity(request, null);
        return featureMapper.toResponse(repository.save(entity));
    }

    @Override
    public void update(String id, UpdateFeatureRequest request) {
        final Feature feature = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature with id " + id + " not found!"));

        if (request.getName() != null) {
            repository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(feature.getId())) {
                    throw new EntityExistsException("Feature with name " + request.getName() + " already exists!");
                }
            });
        }

        featureMapper.patchEntity(feature, request, null);
        repository.save(feature);
    }

    @Override
    public void delete(String id) {
        final Feature feature = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature with id " + id + " not found!"));
        repository.delete(feature);
    }
}
