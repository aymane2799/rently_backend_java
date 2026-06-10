package com.rently.rently.catalog.services;

import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.requests.brand.CreateCarBrandRequest;
import com.rently.rently.catalog.requests.brand.UpdateCarBrandRequest;
import com.rently.rently.shared.CRUDService;

import java.util.List;

public interface CarBrandService extends CRUDService<CreateCarBrandRequest, UpdateCarBrandRequest, CarBrandResponse, String> {
}
