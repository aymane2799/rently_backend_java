package com.rently.rently.catalog.catalogrequests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectCatalogRequestRequest {

    @NotBlank
    private String rejectionReason;
}
