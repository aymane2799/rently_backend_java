package com.rently.rently.fleet.vehicles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateVehicleRequest {
    @NotBlank
    private String licensePlate;

    @NotBlank
    private String insuranceNumber;

    @NotNull
    private LocalDate insuranceExpiresAt;

    private Short year;
    private Short month;
    private String color;
    private Short seats;
    private Short doors;
    private String description;
    private VehicleStatus status;
    private Transmission transmission;
    private FuelType fuelType;
    private BigDecimal dailyBaseRate;

    @NotBlank
    @UUID
    private String modelId;

    private Set<String> featureIds;

    @UUID
    private String currentHubId;

    private String currentParkingSlot;
}
