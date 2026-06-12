package com.rently.rently.agency;

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
                .status(agency.getStatus())
                .approvedAt(agency.getApprovedAt())
                .planId(agency.getPlanId())
                .build();
    }
}
