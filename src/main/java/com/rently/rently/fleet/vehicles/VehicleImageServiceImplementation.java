package com.rently.rently.fleet.vehicles;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleImageServiceImplementation implements VehicleImageService {

    private final VehicleImageRepository imageRepository;
    private final VehicleRepository vehicleRepository;

    @Value("${document.storage.path:./documents}")
    private String storagePath;

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    @Override
    @Transactional
    public VehicleImageResponse upload(String vehicleId, MultipartFile file, String altText) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + vehicleId));

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Image must not exceed 5 MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID() + extension;

        try {
            Path dir = Paths.get(storagePath, "images", vehicleId);
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.write(dest, file.getBytes());

            int nextOrder = imageRepository.findByVehicleIdOrderByDisplayOrder(vehicleId).size();

            VehicleImage image = VehicleImage.builder()
                    .vehicleId(vehicleId)
                    .imageUrl(dest.toString())
                    .isPrimary(false)
                    .displayOrder(nextOrder)
                    .altText(altText)
                    .build();

            VehicleImage saved = imageRepository.save(image);
            return toResponse(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void setPrimary(String vehicleId, String imageId) {
        VehicleImage target = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found: " + imageId));
        if (!target.getVehicleId().equals(vehicleId)) {
            throw new EntityNotFoundException("Image not found for vehicle: " + vehicleId);
        }

        imageRepository.findFirstByVehicleIdAndIsPrimaryTrue(vehicleId)
                .ifPresent(prev -> {
                    prev.setPrimary(false);
                    imageRepository.save(prev);
                });

        target.setPrimary(true);
        imageRepository.save(target);
    }

    @Override
    @Transactional
    public void delete(String vehicleId, String imageId) {
        VehicleImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found: " + imageId));
        if (!image.getVehicleId().equals(vehicleId)) {
            throw new EntityNotFoundException("Image not found for vehicle: " + vehicleId);
        }

        boolean wasPrimary = image.isPrimary();
        imageRepository.delete(image);

        if (wasPrimary) {
            imageRepository.findByVehicleIdOrderByDisplayOrder(vehicleId)
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setPrimary(true);
                        imageRepository.save(next);
                    });
        }
    }

    @Override
    @Transactional
    public void reorder(String vehicleId, List<String> orderedImageIds) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + vehicleId));

        for (int i = 0; i < orderedImageIds.size(); i++) {
            String id = orderedImageIds.get(i);
            VehicleImage image = imageRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Image not found: " + id));
            if (!image.getVehicleId().equals(vehicleId)) {
                throw new EntityNotFoundException("Image " + id + " does not belong to vehicle: " + vehicleId);
            }
            image.setDisplayOrder(i);
            imageRepository.save(image);
        }
    }

    @Override
    public List<VehicleImageResponse> getByVehicleId(String vehicleId) {
        return imageRepository.findByVehicleIdOrderByDisplayOrder(vehicleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VehicleImageResponse toResponse(VehicleImage image) {
        return VehicleImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .isPrimary(image.isPrimary())
                .displayOrder(image.getDisplayOrder())
                .altText(image.getAltText())
                .build();
    }
}
