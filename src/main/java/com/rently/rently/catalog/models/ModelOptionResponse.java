package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;

public record ModelOptionResponse(String id, String name, String brandId, String brandName, VehicleCategory category) {
}
