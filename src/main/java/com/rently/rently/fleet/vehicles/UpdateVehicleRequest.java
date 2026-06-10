package com.rently.rently.fleet.vehicles;

import lombok.*;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateVehicleRequest {
    private String licensePlate;
    private String insuranceNumber;
    private LocalDate insuranceExpiresAt;
    private Short year;
    private Short month;
    private String color;
    private Integer mileage;
    private Short seats;
    private Short doors;
    private String description;
    private VehicleStatus status;
    private Transmission transmission;
    private FuelType fuelType;
    private BigDecimal dailyBaseRate;

    @UUID
    private String modelId;

    private Set<String> featureIds;
}
