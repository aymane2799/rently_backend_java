package com.rently.rently.catalog.features;

import com.rently.rently.shared.CRUDService;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeatureService extends CRUDService<CreateFeatureRequest, UpdateFeatureRequest, FeatureResponse, String> {
    PagedResponse<FeatureResponse> getAll(String search, Boolean isActive, Pageable pageable);
    List<FeatureOptionResponse> getOptions();
}
