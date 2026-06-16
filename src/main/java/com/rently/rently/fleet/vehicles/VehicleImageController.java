package com.rently.rently.fleet.vehicles;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles/{vehicleId}/images")
@RequiredArgsConstructor
public class VehicleImageController {

    private final VehicleImageService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public VehicleImageResponse upload(
            @PathVariable String vehicleId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "altText", required = false) String altText) {
        return service.upload(vehicleId, file, altText);
    }

    @DeleteMapping("{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public void delete(@PathVariable String vehicleId, @PathVariable String imageId) {
        service.delete(vehicleId, imageId);
    }

    @PatchMapping("{imageId}/set-primary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public void setPrimary(@PathVariable String vehicleId, @PathVariable String imageId) {
        service.setPrimary(vehicleId, imageId);
    }

    @PatchMapping("reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public void reorder(@PathVariable String vehicleId, @RequestBody List<String> orderedImageIds) {
        service.reorder(vehicleId, orderedImageIds);
    }
}
