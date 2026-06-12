package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "models",
        uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(
        name = "Model.brand",
        attributeNodes = {
                @NamedAttributeNode("brand"),
        }
)

public class Model extends Auditable {
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private VehicleCategory category;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    //  Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;
}
