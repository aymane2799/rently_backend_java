package com.rently.rently.catalog.services;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.requests.CarBrandRequest;
import com.rently.rently.shared.BasicService;

import java.util.List;

public interface CarBrandService extends BasicService<CarBrandRequest, CarBrandResponse> {
    List<CarBrandResponse> getAll();
    CarBrandResponse get(final String id);
    CarBrandResponse create(final CarBrandRequest request);
    void update(final String id, final CarBrandRequest request);
    void delete(final String id);

}
