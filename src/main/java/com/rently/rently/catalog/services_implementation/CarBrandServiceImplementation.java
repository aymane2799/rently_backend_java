package com.rently.rently.catalog.services_implementation;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.mappers.CarBrandMapper;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.repositories.CarBrandRepository;
import com.rently.rently.catalog.requests.CarBrandRequest;
import com.rently.rently.catalog.services.CarBrandService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CarBrandServiceImplementation implements CarBrandService {
    private final CarBrandRepository repository;
    private final CarBrandMapper carBrandMapper;

    @Override
    public List<CarBrandResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this.carBrandMapper::toResponse)
                .toList();
    }

    @Override
    public CarBrandResponse get(String id) {
        final Optional<CarBrand> brand = Optional.of(this.repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CarBrand with id " + id + " not found!")));

        return this.carBrandMapper.toResponse(brand.get());
    }

    @Override
    public CarBrandResponse create(CarBrandRequest request) {
        final Optional<CarBrand> carBrandExists = this.repository.findByName(request.getName());

        if (carBrandExists.isPresent()) {
            throw new EntityExistsException("CarBrand with name " + request.getName() + " already exists!");
        }

        final CarBrand entity = this.carBrandMapper.toEntity(request);
        final CarBrand createdBrand = this.repository.save(entity);

        return this.carBrandMapper.toResponse(createdBrand);
    }

    @Override
    public void update(@PathVariable String id, CarBrandRequest request) {
        final Optional<CarBrand> brand = this.repository.findById(id);

        if(brand.isEmpty()) {
            throw new EntityNotFoundException("Car Brand not found");
        }

        final CarBrand carBrand = brand.get();

        carBrand.setName(request.getName());
        this.repository.save(carBrand);
    }

    @Override
    public void delete(String id) {
        final  Optional<CarBrand> brand = this.repository.findById(id);

        if(brand.isEmpty()) {throw new EntityNotFoundException("Car Brand not found");}

        this.repository.deleteById(brand.get().getId());
    }

}
