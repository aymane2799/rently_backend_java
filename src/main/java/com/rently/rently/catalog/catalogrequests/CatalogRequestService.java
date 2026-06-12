package com.rently.rently.catalog.catalogrequests;

import java.util.List;

public interface CatalogRequestService {
    CatalogRequestResponse submit(SubmitCatalogRequestRequest request, String agencySlug);
    void approve(String id, String adminUserId);
    void reject(String id, String rejectionReason, String adminUserId);
    List<CatalogRequestResponse> getForAgency(String agencySlug);
    List<CatalogRequestResponse> getAll(CatalogRequestType type, CatalogRequestStatus status);
}
