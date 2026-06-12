package com.rently.rently.reservation.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCustomerRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phone;

    private String email;

    @NotNull
    private IdType idType;

    @NotBlank
    private String idNumber;

    @NotBlank
    private String driverLicenseCode;

    private String address;
}
