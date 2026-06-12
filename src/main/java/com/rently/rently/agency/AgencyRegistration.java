package com.rently.rently.agency;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "agency_registrations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyRegistration extends Auditable {

    @Column(name = "agency_name", nullable = false)
    private String agencyName;

    @Column(name = "rc_number", nullable = false, unique = true)
    private String rcNumber;

    @Column(name = "ice_number", nullable = false, unique = true)
    private String iceNumber;

    @Column(name = "if_number")
    private String ifNumber;

    @Column(name = "patent")
    private String patent;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "website")
    private String website;

    @Column(name = "owner_first_name", nullable = false)
    private String ownerFirstName;

    @Column(name = "owner_last_name", nullable = false)
    private String ownerLastName;

    @Column(name = "owner_email", nullable = false, unique = true)
    private String ownerEmail;

    @Column(name = "owner_phone", nullable = false)
    private String ownerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AgencyRegistrationStatus status = AgencyRegistrationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "resolved_agency_id")
    private String resolvedAgencyId;
}
