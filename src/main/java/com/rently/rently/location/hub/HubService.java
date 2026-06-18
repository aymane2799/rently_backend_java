package com.rently.rently.location.hub;

import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HubService {
    List<HubResponse> getAllByBranch(String branchId);
    PagedResponse<HubResponse> getAllByBranch(String branchId, Boolean active, HubType type, Pageable pageable);
    List<HubOptionResponse> getOptions(String branchId);
    HubResponse get(String id);
    HubResponse create(String branchId, CreateHubRequest request);
    void update(String id, UpdateHubRequest request);
    void deactivate(String id);
}
