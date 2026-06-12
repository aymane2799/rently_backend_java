package com.rently.rently.fleet.vehicles;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends Auditable {
    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(name = "insurance_number", nullable = false, unique = true, length = 20)
    private String insuranceNumber;

    @Column(name = "insurance_expires_at", nullable = false)
    private LocalDate insuranceExpiresAt;

    @Column(columnDefinition = "SMALLINT")
    private Short year;

    @Column(columnDefinition = "SMALLINT")
    private Short month;

    @Column(length = 50)
    private String color;

    @Builder.Default
    @Column(nullable = false)
    private int mileage = 0;

    @Column(columnDefinition = "SMALLINT")
    private Short seats;

    @Column(columnDefinition = "SMALLINT")
    private Short doors;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Transmission transmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Column(name = "daily_base_rate", precision = 10, scale = 2)
    private BigDecimal dailyBaseRate;

    @Column(name = "model_id", nullable = false)
    private String modelId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "vehicle_features",
            joinColumns = @JoinColumn(name = "vehicle_id")
    )
    @Column(name = "feature_id")
    private Set<String> featureIds = new HashSet<>();

    @Column(name = "current_hub_id")
    private String currentHubId;

    @Column(name = "current_parking_slot", length = 100)
    private String currentParkingSlot;
}
