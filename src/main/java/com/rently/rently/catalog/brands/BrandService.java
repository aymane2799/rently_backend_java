package com.rently.rently.catalog.brands;

import com.rently.rently.shared.CRUDService;
import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BrandService extends CRUDService<CreateBrandRequest, UpdateBrandRequest, BrandResponse, String> {
    PagedResponse<BrandResponse> getAll(String search, Boolean isActive, Pageable pageable);
    List<BrandOptionResponse> getOptions();
}
