package com.rently.rently.billing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarkPaidRequest {

    @NotBlank
    private String paymentMode;
}
