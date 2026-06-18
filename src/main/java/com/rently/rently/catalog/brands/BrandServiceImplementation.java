package com.rently.rently.catalog.brands;

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
public class BrandServiceImplementation implements BrandService {
    private final BrandRepository repository;
    private final BrandMapper brandMapper;

    @Override
    public List<BrandResponse> getAll() {
        return repository.findAllByIsActive(true)
                .stream()
                .map(brandMapper::toResponse)
                .toList();
    }

    @Override
    public PagedResponse<BrandResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        Specification<Brand> spec = BrandSpecification.withFilters(search, isActive);
        return PagedResponseMapper.toPagedResponse(repository.findAll(spec, pageable), brandMapper::toResponse);
    }

    @Override
    public List<BrandOptionResponse> getOptions() {
        return repository.findAllByIsActive(true)
                .stream()
                .map(b -> new BrandOptionResponse(b.getId(), b.getName()))
                .toList();
    }

    @Override
    public BrandResponse get(String id) {
        final Brand brand = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id " + id + " not found!"));
        return brandMapper.toResponse(brand);
    }

    @Override
    public BrandResponse create(CreateBrandRequest request) {
        if (repository.findByName(request.getName()).isPresent()) {
            throw new EntityExistsException("Brand with name " + request.getName() + " already exists!");
        }

        final Brand entity = brandMapper.toEntity(request, null);
        return brandMapper.toResponse(repository.save(entity));
    }

    @Override
    public void update(String id, UpdateBrandRequest request) {
        final Brand brand = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id " + id + " not found!"));

        if (request.getName() != null) {
            repository.findByName(request.getName()).ifPresent(existing -> {
                if (!existing.getId().equals(brand.getId())) {
                    throw new EntityExistsException("Brand with name " + request.getName() + " already exists!");
                }
            });
        }

        brandMapper.patchEntity(brand, request, null);
        repository.save(brand);
    }

    @Override
    public void delete(String id) {
        final Brand brand = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand with id " + id + " not found!"));
        brand.setActive(false);
        repository.save(brand);
    }
}
