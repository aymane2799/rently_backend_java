package com.rently.rently.catalog.brands;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandServiceImplementation implements BrandService {
    private final BrandRepository repository;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    @Override
    public BrandResponse get(String id) {
        final Optional<Brand> brand = Optional.of(repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id " + id + " not found!")));

        return brandMapper.toResponse(brand.get());
    }

    @Override
    public BrandResponse create(CreateBrandRequest request) {
        final Optional<Brand> brandExists = repository.findByName(request.getName());

        if (brandExists.isPresent()) {
            throw new EntityExistsException("Brand with name " + request.getName() + " already exists!");
        }

        final Brand entity = brandMapper.toEntity(request,null );
        final Brand createdBrand = repository.save(entity);

        return brandMapper.toResponse(createdBrand);
    }

    @Override
    public void update(@PathVariable String id, UpdateBrandRequest request) {
        final Optional<Brand> brand = repository.findById(id);

        if (brand.isEmpty()) {
            throw new EntityNotFoundException("Car Brand not found");
        }

        final Brand brandEntity = brand.get();

        if (request.getName() != null) {
            final Optional<Brand> brandWithSameName = repository.findByName(request.getName());

            if(brandWithSameName.isPresent() && !brandWithSameName.get().getId().equals(brandEntity.getId())) {
                throw new EntityExistsException("Car Brand with name " + request.getName() + " already exists!");
            }

            brandEntity.setName(request.getName());
        }
        repository.save(brandEntity);
    }

    @Override
    public void delete(String id) {
        final Optional<Brand> brand = repository.findById(id);

        if (brand.isEmpty()) {
            throw new EntityNotFoundException("Car Brand not found");
        }

        repository.deleteById(brand.get().getId());
    }

}
