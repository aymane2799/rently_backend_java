package com.rently.rently.catalog.services;

import com.rently.rently.catalog.reponses.FeatureResponse;
import com.rently.rently.catalog.requests.feature.CreateFeatureRequest;
import com.rently.rently.catalog.requests.feature.UpdateFeatureRequest;
import com.rently.rently.shared.CRUDService;

import java.util.List;

public interface FeatureService extends CRUDService<CreateFeatureRequest, UpdateFeatureRequest, FeatureResponse, String> {
}
