package com.rently.rently.catalog.entities;

import com.rently.rently.catalog.enums.VehicleCategory;
import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "car_models",
        uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarModel extends Auditable {
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VehicleCategory category;

    //  Relationships
    @Column(nullable = false)
    private String brandId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private CarBrand brand;
}
