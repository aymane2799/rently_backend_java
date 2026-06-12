package com.rently.rently.location.hub;

import java.util.List;

public interface HubService {
    List<HubResponse> getAllByBranch(String branchId);
    HubResponse get(String id);
    HubResponse create(String branchId, CreateHubRequest request);
    void update(String id, UpdateHubRequest request);
    void deactivate(String id);
}
