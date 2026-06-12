package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.SubmitRegistrationRequest;

import java.util.List;

public interface AgencyRegistrationService {
    AgencyRegistrationResponse get(String id);
    List<AgencyRegistrationResponse> getAll(AgencyRegistrationStatus status);
    AgencyRegistrationResponse submit(SubmitRegistrationRequest request);
    AgencyRegistrationResponse approve(String id, String reviewedBy);
    AgencyRegistrationResponse reject(String id, String rejectionReason, String reviewedBy);
}
