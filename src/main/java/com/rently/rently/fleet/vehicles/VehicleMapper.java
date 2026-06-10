package com.rently.rently.fleet.vehicles;

import com.rently.rently.catalog.features.FeatureMapper;
import com.rently.rently.catalog.features.FeatureResponse;
import com.rently.rently.catalog.models.ModelMapper;
import com.rently.rently.fleet.vehicles.hydration.VehicleHydrationContext;
import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VehicleMapper implements
        ResponseMapper<Vehicle, VehicleResponse>,
        CreateMapper<Vehicle, CreateVehicleRequest, VehicleHydrationContext>,
        PatchMapper<Vehicle, UpdateVehicleRequest, VehicleHydrationContext> {

    private final ModelMapper modelMapper;
    private final FeatureMapper featureMapper;

    @Override
    public Vehicle toEntity(CreateVehicleRequest request, VehicleHydrationContext ctx) {
        return Vehicle.builder()
                .licensePlate(request.getLicensePlate())
                .insuranceNumber(request.getInsuranceNumber())
                .insuranceExpiresAt(request.getInsuranceExpiresAt())
                .year(request.getYear())
                .month(request.getMonth())
                .color(request.getColor())
                .seats(request.getSeats())
                .doors(request.getDoors())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : VehicleStatus.AVAILABLE)
                .transmission(request.getTransmission())
                .fuelType(request.getFuelType())
                .dailyBaseRate(request.getDailyBaseRate())
                .model(ctx.getModel())
                .features(ctx.getFeatures() != null ? ctx.getFeatures() : Set.of())
                .build();
    }

    @Override
    public void patchEntity(Vehicle entity, UpdateVehicleRequest request, VehicleHydrationContext ctx) {
        if (request.getLicensePlate() != null) entity.setLicensePlate(request.getLicensePlate());
        if (request.getInsuranceNumber() != null) entity.setInsuranceNumber(request.getInsuranceNumber());
        if (request.getInsuranceExpiresAt() != null) entity.setInsuranceExpiresAt(request.getInsuranceExpiresAt());
        if (request.getYear() != null) entity.setYear(request.getYear());
        if (request.getMonth() != null) entity.setMonth(request.getMonth());
        if (request.getColor() != null) entity.setColor(request.getColor());
        if (request.getMileage() != null) entity.setMileage(request.getMileage());
        if (request.getSeats() != null) entity.setSeats(request.getSeats());
        if (request.getDoors() != null) entity.setDoors(request.getDoors());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getTransmission() != null) entity.setTransmission(request.getTransmission());
        if (request.getFuelType() != null) entity.setFuelType(request.getFuelType());
        if (request.getDailyBaseRate() != null) entity.setDailyBaseRate(request.getDailyBaseRate());
        if (ctx.getModel() != null) entity.setModel(ctx.getModel());
        if (ctx.getFeatures() != null) entity.setFeatures(ctx.getFeatures());
    }

    @Override
    public VehicleResponse toResponse(Vehicle entity) {
        final Set<FeatureResponse> features = entity.getFeatures() != null
                ? entity.getFeatures().stream().map(featureMapper::toResponse).collect(Collectors.toSet())
                : Set.of();

        return VehicleResponse.builder()
                .id(entity.getId())
                .licensePlate(entity.getLicensePlate())
                .insuranceNumber(entity.getInsuranceNumber())
                .insuranceExpiresAt(entity.getInsuranceExpiresAt())
                .year(entity.getYear())
                .month(entity.getMonth())
                .color(entity.getColor())
                .mileage(entity.getMileage())
                .seats(entity.getSeats())
                .doors(entity.getDoors())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .transmission(entity.getTransmission())
                .fuelType(entity.getFuelType())
                .dailyBaseRate(entity.getDailyBaseRate())
                .model(modelMapper.toResponse(entity.getModel()))
                .features(features)
                .build();
    }
}
