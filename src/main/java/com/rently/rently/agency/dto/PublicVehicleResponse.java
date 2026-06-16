package com.rently.rently.agency.dto;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.catalog.features.FeatureResponse;
import com.rently.rently.fleet.vehicles.FuelType;
import com.rently.rently.fleet.vehicles.Transmission;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class PublicVehicleResponse {
    private String id;
    private String modelName;
    private String brandName;
    private VehicleCategory category;
    private Transmission transmission;
    private FuelType fuelType;
    private BigDecimal dailyBaseRate;
    private Short seats;
    private Short year;
    private String color;
    private Set<FeatureResponse> features;
    private String primaryImageUrl;
}
