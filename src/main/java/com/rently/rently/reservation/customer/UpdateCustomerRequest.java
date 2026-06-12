package com.rently.rently.reservation.customer;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomerRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private IdType idType;
    private String idNumber;
    private String driverLicenseCode;
    private String address;
}
