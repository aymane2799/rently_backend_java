package com.rently.rently.catalog.catalogrequests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogRequestRepository extends JpaRepository<CatalogRequest, String> {
    List<CatalogRequest> findByAgencySlug(String agencySlug);
    List<CatalogRequest> findAllByStatus(CatalogRequestStatus status);
    List<CatalogRequest> findAllByTypeAndStatus(CatalogRequestType type, CatalogRequestStatus status);
    List<CatalogRequest> findAllByType(CatalogRequestType type);
}
