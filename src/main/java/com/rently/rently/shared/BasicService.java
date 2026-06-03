package com.rently.rently.shared;

import com.rently.rently.catalog.entities.CarBrand;
import com.rently.rently.catalog.requests.CarBrandRequest;

import java.util.List;

public interface BasicService<I, O> {
    List<O> getAll();
    O get(String id);
    O create(I request);
    void update(String id, I request);
    void delete(String id);
}
