package com.rently.rently.catalog.features;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService service;

    @GetMapping
    public List<FeatureResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public FeatureResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
