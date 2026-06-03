package com.rently.rently.catalog.services_implementation;

import com.rently.rently.catalog.entities.CarModel;
import com.rently.rently.catalog.mappers.CarModelMapper;
import com.rently.rently.catalog.reponses.CarModelResponse;
import com.rently.rently.catalog.repositories.CarModelRepository;
import com.rently.rently.catalog.requests.CarModelRequest;
import com.rently.rently.catalog.services.CarModelService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CarModelServiceImplementation implements CarModelService {
    private final CarModelRepository repository;
    private final CarModelMapper carModelMapper;

    @Override
    public List<CarModelResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this.carModelMapper::toResponse)
                .toList();
    }

    @Override
    public CarModelResponse get(String id) {
        final Optional<CarModel> model = Optional.of(
                this.repository.findById(id)
                        .orElseThrow(()-> new EntityNotFoundException("Car Model with id "+ id + " not found!" )
                        )
        );

        return this.carModelMapper.toResponse(model.get());
    }

    @Override
    public CarModelResponse create(CarModelRequest request) {
        final Optional<CarModel> carModelExists = this.repository.findByName(request.getName());

        if (carModelExists.isPresent()) {
            throw new EntityExistsException("Car Model with name " + request.getName() + " already exists!");
        }

        final CarModel entity = this.carModelMapper.toEntity(request);
        final CarModel createdModel = this.repository.save(entity);

        return this.carModelMapper.toResponse(createdModel);
    }

    @Override
    public void update(@PathVariable String id, CarModelRequest request) {
        final Optional<CarModel> model = this.repository.findById(id);

        if(model.isEmpty()) {
            throw new EntityNotFoundException("Car Model not found");
        }

        final CarModel carModel = model.get();

        carModel.setName(request.getName());
        this.repository.save(carModel);
    }

    @Override
    public void delete(String id) {
        final  Optional<CarModel> model = this.repository.findById(id);

        if(model.isEmpty()) {throw new EntityNotFoundException("Car Model not found");}

        this.repository.deleteById(model.get().getId());
    }

}
