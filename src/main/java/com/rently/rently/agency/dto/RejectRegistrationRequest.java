package com.rently.rently.agency.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRegistrationRequest {

    @NotBlank
    private String rejectionReason;
}
