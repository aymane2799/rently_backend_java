package com.rently.rently.agency.dto;

import com.rently.rently.agency.AgencyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AgencyResponse {
    private String id;
    private String name;
    private String slug;
    private String rcNumber;
    private String iceNumber;
    private String ifNumber;
    private String patent;
    private String ownerFirstName;
    private String ownerLastName;
    private String phone;
    private String email;
    private String city;
    private String address;
    private String website;
    private String logo;
    private String coverImage;
    private AgencyStatus status;
    private Instant approvedAt;
    private String planId;
}
