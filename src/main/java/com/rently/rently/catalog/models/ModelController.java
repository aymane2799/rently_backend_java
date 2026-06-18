package com.rently.rently.catalog.models;

import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService service;

    @GetMapping
    public PagedResponse<ModelResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) VehicleCategory category,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAll(search, brandId, category, isActive, pageable);
    }

    @GetMapping("/options")
    public List<ModelOptionResponse> getOptions(
            @RequestParam(required = false) String brandId
    ) {
        return service.getOptions(brandId);
    }

    @GetMapping("/{id}")
    public ModelResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
