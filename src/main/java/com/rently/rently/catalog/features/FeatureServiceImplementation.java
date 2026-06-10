package com.rently.rently.catalog.features;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        final Optional<Feature> feature = Optional.of(repository.findById(id))
                .orElseThrow(() -> new EntityNotFoundException(("Feature with id " + id + " not found!")));

        return featureMapper.toResponse(feature.get());
    }

    @Override
    public FeatureResponse create(CreateFeatureRequest request) {
        final Optional<Feature> featureExists = repository.findByName(request.getName());

        if (featureExists.isPresent()) {
            throw new EntityExistsException("Feature with name " + request.getName() + " already exists!");
        }

        final Feature entity = featureMapper.toEntity(request, null);
        final Feature createdBrand = repository.save(entity);

        return featureMapper.toResponse(createdBrand);
    }


    @Override
    public void update(String id, UpdateFeatureRequest request) {
        final Optional<Feature> featureExists = repository.findById(id);

        if (featureExists.isEmpty()) {
            throw new EntityExistsException("Feature not found!");
        }

        final Feature feature = featureExists.get();

//        if (feature.getName() != null) {
//            final Optional<Feature> existingBrand = repository.findByName(request.getName());
//
//            if (existingBrand.isPresent() && !existingBrand.get().getId().equals(feature.getId())) {
//                throw new EntityExistsException("Feature with name " + request.getName() + " already exists!");
//            }
//
//            feature.setName(request.getName());
//        }
//
//        if (feature.getIcon() != null) {
//            feature.setIcon(request.getIcon());
//        }
//
//        if (feature.getDescription() != null) {
//            feature.setDescription(request.getDescription());
//        }

        repository.save(feature);
    }

    @Override
    public void delete(String id) {
        final Optional<Feature> featureExists = repository.findById(id);

        if (featureExists.isEmpty()) {
            throw new EntityNotFoundException("Feature not found!");
        }

        repository.delete(featureExists.get());
    }
}
