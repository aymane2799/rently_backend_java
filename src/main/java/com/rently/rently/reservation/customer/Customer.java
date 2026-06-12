package com.rently.rently.reservation.customer;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_type", "id_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends Auditable {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IdType idType;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "driver_license_code", nullable = false)
    private String driverLicenseCode;

    @Column
    private String address;
}
