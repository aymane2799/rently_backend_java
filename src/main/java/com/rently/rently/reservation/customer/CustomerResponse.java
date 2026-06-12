package com.rently.rently.reservation.customer;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private IdType idType;
    private String idNumber;
    private String driverLicenseCode;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;
}
