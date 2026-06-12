package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import org.springframework.stereotype.Component;

@Component
public class AgencyRegistrationMapper {

    public AgencyRegistrationResponse toResponse(AgencyRegistration registration) {
        return AgencyRegistrationResponse.builder()
                .id(registration.getId())
                .agencyName(registration.getAgencyName())
                .rcNumber(registration.getRcNumber())
                .iceNumber(registration.getIceNumber())
                .ifNumber(registration.getIfNumber())
                .patent(registration.getPatent())
                .city(registration.getCity())
                .address(registration.getAddress())
                .website(registration.getWebsite())
                .ownerFirstName(registration.getOwnerFirstName())
                .ownerLastName(registration.getOwnerLastName())
                .ownerEmail(registration.getOwnerEmail())
                .ownerPhone(registration.getOwnerPhone())
                .status(registration.getStatus())
                .rejectionReason(registration.getRejectionReason())
                .submittedAt(registration.getSubmittedAt())
                .reviewedAt(registration.getReviewedAt())
                .reviewedBy(registration.getReviewedBy())
                .resolvedAgencyId(registration.getResolvedAgencyId())
                .build();
    }
}
