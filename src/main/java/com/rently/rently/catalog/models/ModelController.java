package com.rently.rently.catalog.models;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("{id}")
    public ModelResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ModelResponse create(@RequestBody @Valid CreateModelRequest request) {
        return service.create(request);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid UpdateModelRequest request) {
        service.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
