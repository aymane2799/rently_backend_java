package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.catalog.VehicleCategory;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatalogRequestResponse {
    private String id;
    private String agencySlug;
    private CatalogRequestType type;
    private CatalogRequestStatus status;
    private String proposedName;
    private String proposedBrandId;
    private String proposedBrandName;
    private VehicleCategory proposedCategory;
    private String proposedIcon;
    private String proposedDescription;
    private String notes;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant reviewedAt;
    private String reviewedBy;
    private String resolvedEntityId;
}
