package com.rently.rently.fleet.vehicles;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VehicleImageService {
    VehicleImageResponse upload(String vehicleId, MultipartFile file, String altText);
    void setPrimary(String vehicleId, String imageId);
    void delete(String vehicleId, String imageId);
    void reorder(String vehicleId, List<String> orderedImageIds);
    List<VehicleImageResponse> getByVehicleId(String vehicleId);
}
