package com.rently.rently.catalog.services_implementation;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.entities.CarModel;
import com.rently.rently.catalog.mappers.CarModelMapper;
import com.rently.rently.catalog.reponses.CarModelResponse;
import com.rently.rently.catalog.repositories.CarBrandRepository;
import com.rently.rently.catalog.repositories.CarModelRepository;
import com.rently.rently.catalog.requests.brand.UpdateCarBrandRequest;
import com.rently.rently.catalog.requests.model.CreateCarModelRequest;
import com.rently.rently.catalog.requests.model.UpdateCarModelRequest;
import com.rently.rently.catalog.services.CarModelService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarModelServiceImplementation implements CarModelService {
    private final CarModelRepository repository;
    private final CarModelMapper carModelMapper;
    private final CarBrandRepository carBrandRepository;

    @Override
    public List<CarModelResponse> getAll() {
        System.out.println("getAll");

        return repository.findAll()
                .stream()
                .map(carModelMapper::toResponse)
                .toList();
    }

    @Override
    public CarModelResponse get(String id) {
        final Optional<CarModel> model = Optional.of(
                repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Car Model with id " + id + " not found!")
                        )
        );

        return carModelMapper.toResponse(model.get());
    }

    @Override
    public CarModelResponse create(CreateCarModelRequest request) {
        final Optional<CarModel> carModelExists = repository.findByName(request.getName());

        if (carModelExists.isPresent()) {
            throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
        }

        final Optional<CarBrand> brand = carBrandRepository.findById(request.getBrandId());
        if (brand.isEmpty()) {
            throw new EntityNotFoundException("Brand with id " + request.getBrandId() + " not found!");
        }

        final CarModel entity = carModelMapper.toEntity(request, brand.get());

        entity.setBrand(brand.get());

        final CarModel createdModel = repository.save(entity);

        return carModelMapper.toResponse(createdModel);
    }

    @Override
    public void update(@PathVariable String id, UpdateCarModelRequest request) {
        final Optional<CarModel> model = repository.findById(id);

        if (model.isEmpty()) {
            throw new EntityNotFoundException("Car Model not found");
        }

        final CarModel carModel = model.get();

        if (request.getBrandId() != null) {
            final CarBrand brand = carBrandRepository.getReferenceById(request.getBrandId());
            carModel.setBrand(brand);
        }

        if (request.getName() != null) {
            final Optional<CarModel> modelWithSameName = repository.findByName(request.getName());

            if (modelWithSameName.isPresent() && !modelWithSameName.get().getId().equals(carModel.getId()) ) {
                throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
            }

            carModel.setName(request.getName());
        }

        repository.save(carModel);
    }

    @Override
    public void delete(String id) {
        final Optional<CarModel> model = repository.findById(id);

        if (model.isEmpty()) {
            throw new EntityNotFoundException("Car Model not found");
        }

        repository.deleteById(model.get().getId());
    }

}
