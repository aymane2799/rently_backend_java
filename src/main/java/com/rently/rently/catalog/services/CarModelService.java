package com.rently.rently.catalog.services;

import com.rently.rently.catalog.reponses.CarModelResponse;
import com.rently.rently.catalog.requests.model.CreateCarModelRequest;
import com.rently.rently.catalog.requests.model.UpdateCarModelRequest;
import com.rently.rently.shared.CRUDService;

public interface CarModelService extends CRUDService<CreateCarModelRequest, UpdateCarModelRequest, CarModelResponse, String> {
}
