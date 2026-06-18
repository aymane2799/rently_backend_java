package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.SubmitRegistrationRequest;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AgencyRegistrationService {
    AgencyRegistrationResponse get(String id);
    List<AgencyRegistrationResponse> getAll(AgencyRegistrationStatus status);
    PagedResponse<AgencyRegistrationResponse> getAll(AgencyRegistrationStatus status, String city, String search, Pageable pageable);
    AgencyRegistrationResponse submit(SubmitRegistrationRequest request);
    AgencyRegistrationResponse approve(String id, String reviewedBy);
    AgencyRegistrationResponse reject(String id, String rejectionReason, String reviewedBy);
}
