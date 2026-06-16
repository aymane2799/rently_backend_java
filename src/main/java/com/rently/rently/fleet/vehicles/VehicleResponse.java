package com.rently.rently.fleet.vehicles;

import com.rently.rently.catalog.features.FeatureResponse;
import com.rently.rently.catalog.models.ModelResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleResponse {
    private String id;
    private String licensePlate;
    private String insuranceNumber;
    private LocalDate insuranceExpiresAt;
    private Short year;
    private Short month;
    private String color;
    private int mileage;
    private Short seats;
    private Short doors;
    private String description;
    private VehicleStatus status;
    private Transmission transmission;
    private FuelType fuelType;
    private BigDecimal dailyBaseRate;
    private ModelResponse model;
    private Set<FeatureResponse> features;
    private String currentHubId;
    private String currentParkingSlot;
    private List<VehicleImageResponse> images;
}
