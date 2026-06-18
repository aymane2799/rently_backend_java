package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CatalogRequestService {
    CatalogRequestResponse submit(SubmitCatalogRequestRequest request, String agencySlug);
    void approve(String id, String adminUserId);
    void reject(String id, String rejectionReason, String adminUserId);
    List<CatalogRequestResponse> getForAgency(String agencySlug);
    List<CatalogRequestResponse> getAll(CatalogRequestType type, CatalogRequestStatus status);
    PagedResponse<CatalogRequestResponse> getAll(CatalogRequestType type, CatalogRequestStatus status, String agencySlug, Pageable pageable);
}
