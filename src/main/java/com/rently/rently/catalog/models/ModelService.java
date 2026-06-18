package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.shared.CRUDService;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ModelService extends CRUDService<CreateModelRequest, UpdateModelRequest, ModelResponse, String> {
    PagedResponse<ModelResponse> getAll(String search, String brandId, VehicleCategory category, Boolean isActive, Pageable pageable);
    List<ModelOptionResponse> getOptions(String brandId);
}
