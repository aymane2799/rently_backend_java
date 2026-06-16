package com.rently.rently.agency.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyPublicProfileResponse {
    private String id;
    private String name;
    private String slug;
    private String logo;
    private String coverImage;
    private String tagline;
    private String primaryColor;
    private String secondaryColor;
    private String darkPrimaryColor;
    private String darkSecondaryColor;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String ogImageUrl;
    private String city;
    private String website;
    private String phone;
}
