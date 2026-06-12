package com.rently.rently.agency.dto;

import com.rently.rently.agency.AgencyRegistrationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AgencyRegistrationResponse {
    private String id;
    private String agencyName;
    private String rcNumber;
    private String iceNumber;
    private String ifNumber;
    private String patent;
    private String city;
    private String address;
    private String website;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerEmail;
    private String ownerPhone;
    private AgencyRegistrationStatus status;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant reviewedAt;
    private String reviewedBy;
    private String resolvedAgencyId;
}
