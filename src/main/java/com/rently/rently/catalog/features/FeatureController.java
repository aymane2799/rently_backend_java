package com.rently.rently.catalog.features;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {
    final FeatureService service;

    @GetMapping
    public List<FeatureResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public FeatureResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureResponse create(@RequestBody @Valid CreateFeatureRequest request) {
        return service.create(request);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid UpdateFeatureRequest request) {
        service.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
