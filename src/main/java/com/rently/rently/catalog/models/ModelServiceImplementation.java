package com.rently.rently.catalog.models;

import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.catalog.brands.BrandRepository;
import com.rently.rently.catalog.models.hydration.ModelHydrationContext;
import com.rently.rently.catalog.models.hydration.ModelHydrator;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModelServiceImplementation implements ModelService {
    private final ModelRepository repository;
    private final ModelMapper modelMapper;
    private final BrandRepository brandRepository;
    private final ModelHydrator hydrator;

    @Override
    public List<ModelResponse> getAll() {
        System.out.println("getAll");

        return repository.findAll()
                .stream()
                .map(modelMapper::toResponse)
                .toList();
    }

    @Override
    public ModelResponse get(String id) {
        final Optional<Model> model = Optional.of(
                repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Car Model with id " + id + " not found!")
                        )
        );

        return modelMapper.toResponse(model.get());
    }

    @Override
    public ModelResponse create(CreateModelRequest request) {
        final Optional<Model> modelExists = repository.findByName(request.getName());

        if (modelExists.isPresent()) {
            throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
        }

        final Optional<Brand> brand = brandRepository.findById(request.getBrandId());
        if (brand.isEmpty()) {
            throw new EntityNotFoundException("Brand with id " + request.getBrandId() + " not found!");
        }

        ModelHydrationContext context = hydrator.hydrate(request);

        final Model entity = modelMapper.toEntity(request, context);

        entity.setBrand(brand.get());

        final Model createdModel = repository.save(entity);

        return modelMapper.toResponse(createdModel);
    }

    @Override
    public void update(@PathVariable String id, UpdateModelRequest request) {
        final Optional<Model> model = repository.findById(id);

        if (model.isEmpty()) {
            throw new EntityNotFoundException("Model not found");
        }

        final Model modelEntity = model.get();

        if (request.getBrandId() != null) {
            final Brand brand = brandRepository.getReferenceById(request.getBrandId());
            modelEntity.setBrand(brand);
        }

        if (request.getName() != null) {
            final Optional<Model> modelWithSameName = repository.findByName(request.getName());

            if (modelWithSameName.isPresent() && !modelWithSameName.get().getId().equals(modelEntity.getId()) ) {
                throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
            }

            modelEntity.setName(request.getName());
        }

        repository.save(modelEntity);
    }

    @Override
    public void delete(String id) {
        final Optional<Model> model = repository.findById(id);

        if (model.isEmpty()) {
            throw new EntityNotFoundException("Car Model not found");
        }

        repository.deleteById(model.get().getId());
    }

}
