package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "catalog_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogRequest extends Auditable {

    @Column(name = "agency_slug", nullable = false)
    private String agencySlug;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CatalogRequestType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CatalogRequestStatus status = CatalogRequestStatus.PENDING;

    @Column(name = "proposed_name", nullable = false, length = 100)
    private String proposedName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_brand_id")
    private Brand proposedBrand;

    @Column(name = "proposed_brand_name", length = 100)
    private String proposedBrandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposed_category", length = 20)
    private VehicleCategory proposedCategory;

    @Column(name = "proposed_icon", length = 100)
    private String proposedIcon;

    @Column(name = "proposed_description", columnDefinition = "TEXT")
    private String proposedDescription;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "resolved_entity_id")
    private String resolvedEntityId;
}
