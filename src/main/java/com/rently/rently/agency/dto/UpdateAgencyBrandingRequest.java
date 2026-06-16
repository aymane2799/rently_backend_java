package com.rently.rently.agency.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAgencyBrandingRequest {

    @Size(max = 200)
    private String tagline;

    @Size(max = 7)
    private String primaryColor;

    @Size(max = 7)
    private String secondaryColor;

    @Size(max = 7)
    private String darkPrimaryColor;

    @Size(max = 7)
    private String darkSecondaryColor;

    @Size(max = 70)
    private String metaTitle;

    @Size(max = 160)
    private String metaDescription;

    private String metaKeywords;

    private String ogImageUrl;
}
