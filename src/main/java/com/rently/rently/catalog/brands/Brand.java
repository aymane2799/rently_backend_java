package com.rently.rently.catalog.brands;

import jakarta.persistence.*;
import lombok.*;
import com.rently.rently.shared.Auditable;

@Entity
@Table(name = "car_brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand extends Auditable {
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
