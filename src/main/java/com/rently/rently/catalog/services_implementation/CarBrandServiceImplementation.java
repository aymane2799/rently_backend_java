package com.rently.rently.catalog.services_implementation;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.mappers.CarBrandMapper;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.repositories.CarBrandRepository;
import com.rently.rently.catalog.requests.brand.CreateCarBrandRequest;
import com.rently.rently.catalog.requests.brand.UpdateCarBrandRequest;
import com.rently.rently.catalog.services.CarBrandService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarBrandServiceImplementation implements CarBrandService {
    private final CarBrandRepository repository;
    private final CarBrandMapper carBrandMapper;

    @Override
    public List<CarBrandResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(carBrandMapper::toResponse)
                .toList();
    }

    @Override
    public CarBrandResponse get(String id) {
        final Optional<CarBrand> brand = Optional.of(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CarBrand with id " + id + " not found!")));

        return carBrandMapper.toResponse(brand.get());
    }

    @Override
    public CarBrandResponse create(CreateCarBrandRequest request) {
        final Optional<CarBrand> carBrandExists = repository.findByName(request.getName());

        if (carBrandExists.isPresent()) {
            throw new EntityExistsException("CarBrand with name " + request.getName() + " already exists!");
        }

        final CarBrand entity = carBrandMapper.toEntity(request);
        final CarBrand createdBrand = repository.save(entity);

        return carBrandMapper.toResponse(createdBrand);
    }

    @Override
    public void update(@PathVariable String id, UpdateCarBrandRequest request) {
        final Optional<CarBrand> brand = repository.findById(id);

        if (brand.isEmpty()) {
            throw new EntityNotFoundException("Car Brand not found");
        }

        final CarBrand carBrand = brand.get();

        if (request.getName() != null) {
            final Optional<CarBrand> brandWithSameName = repository.findByName(request.getName());

            if(brandWithSameName.isPresent() && !brandWithSameName.get().getId().equals(carBrand.getId())) {
                throw new EntityExistsException("Car Brand with name " + request.getName() + " already exists!");
            }

            carBrand.setName(request.getName());
        }
        repository.save(carBrand);
    }

    @Override
    public void delete(String id) {
        final Optional<CarBrand> brand = repository.findById(id);

        if (brand.isEmpty()) {
            throw new EntityNotFoundException("Car Brand not found");
        }

        repository.deleteById(brand.get().getId());
    }

}
