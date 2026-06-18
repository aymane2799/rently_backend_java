package com.rently.rently.catalog.features;

import com.rently.rently.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService service;

    @GetMapping
    public PagedResponse<FeatureResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAll(search, isActive, pageable);
    }

    @GetMapping("/options")
    public List<FeatureOptionResponse> getOptions() {
        return service.getOptions();
    }

    @GetMapping("/{id}")
    public FeatureResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
