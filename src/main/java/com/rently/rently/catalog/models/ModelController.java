package com.rently.rently.catalog.models;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelService service;

    @GetMapping
    public List<ModelResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ModelResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
