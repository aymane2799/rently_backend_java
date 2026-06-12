package com.rently.rently.fleet.vehicles;

import com.rently.rently.catalog.PublicCatalogService;
import com.rently.rently.fleet.vehicles.hydration.VehicleHydrationContext;
import com.rently.rently.fleet.vehicles.hydration.VehicleHydrator;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleServiceImplementation implements VehicleService {
    private final VehicleRepository repository;
    private final VehicleMapper vehicleMapper;
    private final VehicleHydrator hydrator;
    private final PublicCatalogService publicCatalogService;

    @Override
    public List<VehicleResponse> getAll() {
        return repository.findAll().stream().map(vehicleMapper::toResponse).toList();
    }

    @Override
    public VehicleResponse get(String id) {
        final Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + id + " not found!"));
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    public VehicleResponse create(CreateVehicleRequest request) {
        if (repository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new EntityExistsException("Vehicle with license plate " + request.getLicensePlate() + " already exists!");
        }
        if (repository.findByInsuranceNumber(request.getInsuranceNumber()).isPresent()) {
            throw new EntityExistsException("Vehicle with insurance number " + request.getInsuranceNumber() + " already exists!");
        }

        if (!publicCatalogService.modelExists(request.getModelId())) {
            throw new EntityNotFoundException("Model not found: " + request.getModelId());
        }

        Set<String> featureIds = request.getFeatureIds();
        if (featureIds != null && !featureIds.isEmpty() && !publicCatalogService.allFeaturesExist(featureIds)) {
            throw new EntityNotFoundException("One or more features not found");
        }

        final VehicleHydrationContext context = hydrator.hydrate(request);
        return vehicleMapper.toResponse(repository.save(vehicleMapper.toEntity(request, context)));
    }

    @Override
    public void update(String id, UpdateVehicleRequest request) {
        final Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + id + " not found!"));

        if (request.getLicensePlate() != null) {
            repository.findByLicensePlate(request.getLicensePlate()).ifPresent(existing -> {
                if (!existing.getId().equals(vehicle.getId())) {
                    throw new EntityExistsException("Vehicle with license plate " + request.getLicensePlate() + " already exists!");
                }
            });
        }

        if (request.getInsuranceNumber() != null) {
            repository.findByInsuranceNumber(request.getInsuranceNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(vehicle.getId())) {
                    throw new EntityExistsException("Vehicle with insurance number " + request.getInsuranceNumber() + " already exists!");
                }
            });
        }

        if (request.getModelId() != null && !publicCatalogService.modelExists(request.getModelId())) {
            throw new EntityNotFoundException("Model not found: " + request.getModelId());
        }

        Set<String> featureIds = request.getFeatureIds();
        if (featureIds != null && !featureIds.isEmpty() && !publicCatalogService.allFeaturesExist(featureIds)) {
            throw new EntityNotFoundException("One or more features not found");
        }

        final VehicleHydrationContext context = hydrator.hydrate(request);
        vehicleMapper.patchEntity(vehicle, request, context);
        repository.save(vehicle);
    }

    @Override
    public void delete(String id) {
        final Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with id " + id + " not found!"));
        repository.delete(vehicle);
    }
}
