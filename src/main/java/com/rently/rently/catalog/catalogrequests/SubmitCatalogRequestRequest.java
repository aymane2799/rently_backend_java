package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.catalog.VehicleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubmitCatalogRequestRequest {

    @NotNull
    private CatalogRequestType type;

    @NotBlank
    private String proposedName;

    private String proposedBrandId;

    private String proposedBrandName;

    private VehicleCategory proposedCategory;

    private String proposedIcon;

    private String proposedDescription;

    private String notes;
}
