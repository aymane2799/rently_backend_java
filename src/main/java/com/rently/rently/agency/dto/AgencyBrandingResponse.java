package com.rently.rently.agency.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgencyBrandingResponse {
    private String tagline;
    private String primaryColor;
    private String secondaryColor;
    private String darkPrimaryColor;
    private String darkSecondaryColor;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private String ogImageUrl;
}
