package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyBrandingResponse;
import com.rently.rently.agency.dto.AgencyPublicProfileResponse;
import com.rently.rently.agency.dto.AgencyResponse;
import org.springframework.stereotype.Component;

@Component
public class AgencyMapper {

    public AgencyResponse toResponse(Agency agency) {
        return AgencyResponse.builder()
                .id(agency.getId())
                .name(agency.getName())
                .slug(agency.getSlug())
                .rcNumber(agency.getRcNumber())
                .iceNumber(agency.getIceNumber())
                .ifNumber(agency.getIfNumber())
                .patent(agency.getPatent())
                .ownerFirstName(agency.getOwnerFirstName())
                .ownerLastName(agency.getOwnerLastName())
                .phone(agency.getPhone())
                .email(agency.getEmail())
                .city(agency.getCity())
                .address(agency.getAddress())
                .website(agency.getWebsite())
                .logo(agency.getLogo())
                .coverImage(agency.getCoverImage())
                .tagline(agency.getTagline())
                .primaryColor(agency.getPrimaryColor())
                .secondaryColor(agency.getSecondaryColor())
                .darkPrimaryColor(agency.getDarkPrimaryColor())
                .darkSecondaryColor(agency.getDarkSecondaryColor())
                .metaTitle(agency.getMetaTitle())
                .metaDescription(agency.getMetaDescription())
                .metaKeywords(agency.getMetaKeywords())
                .ogImageUrl(agency.getOgImageUrl())
                .status(agency.getStatus())
                .approvedAt(agency.getApprovedAt())
                .planId(agency.getPlanId())
                .build();
    }

    public AgencyPublicProfileResponse toPublicProfileResponse(Agency agency) {
        return AgencyPublicProfileResponse.builder()
                .id(agency.getId())
                .name(agency.getName())
                .slug(agency.getSlug())
                .logo(agency.getLogo())
                .coverImage(agency.getCoverImage())
                .tagline(agency.getTagline())
                .primaryColor(agency.getPrimaryColor())
                .secondaryColor(agency.getSecondaryColor())
                .darkPrimaryColor(agency.getDarkPrimaryColor())
                .darkSecondaryColor(agency.getDarkSecondaryColor())
                .metaTitle(agency.getMetaTitle())
                .metaDescription(agency.getMetaDescription())
                .metaKeywords(agency.getMetaKeywords())
                .ogImageUrl(agency.getOgImageUrl())
                .city(agency.getCity())
                .website(agency.getWebsite())
                .phone(agency.getPhone())
                .build();
    }

    public AgencyBrandingResponse toBrandingResponse(Agency agency) {
        return AgencyBrandingResponse.builder()
                .tagline(agency.getTagline())
                .primaryColor(agency.getPrimaryColor())
                .secondaryColor(agency.getSecondaryColor())
                .darkPrimaryColor(agency.getDarkPrimaryColor())
                .darkSecondaryColor(agency.getDarkSecondaryColor())
                .metaTitle(agency.getMetaTitle())
                .metaDescription(agency.getMetaDescription())
                .metaKeywords(agency.getMetaKeywords())
                .ogImageUrl(agency.getOgImageUrl())
                .build();
    }
}
