package com.rently.rently.catalog.entities;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feature extends Auditable {
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String icon;

    @Column(columnDefinition = "TEXT")
    private String description;

}
