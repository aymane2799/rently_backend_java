package com.rently.rently.agency.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitRegistrationRequest {

    @NotBlank
    private String agencyName;

    @NotBlank
    private String rcNumber;

    @NotBlank
    private String iceNumber;

    private String ifNumber;
    private String patent;

    @NotBlank
    private String city;

    private String address;
    private String website;

    @NotBlank
    private String ownerFirstName;

    @NotBlank
    private String ownerLastName;

    @NotBlank
    @Email
    private String ownerEmail;

    @NotBlank
    private String ownerPhone;
}
