package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyBrandingResponse;
import com.rently.rently.agency.dto.AgencyResponse;
import com.rently.rently.agency.dto.UpdateAgencyBrandingRequest;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AgencyService {
    AgencyResponse get(String id);
    List<AgencyResponse> getAll(AgencyStatus status);
    PagedResponse<AgencyResponse> getAll(AgencyStatus status, String city, String planId, String search, Pageable pageable);
    AgencyResponse block(String id);
    AgencyResponse unblock(String id);
    AgencyBrandingResponse getBranding(String agencySlug);
    void updateBranding(String agencySlug, UpdateAgencyBrandingRequest request);
}
