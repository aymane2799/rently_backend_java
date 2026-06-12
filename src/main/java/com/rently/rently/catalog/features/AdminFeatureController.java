package com.rently.rently.catalog.features;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/features")
@RequiredArgsConstructor
public class AdminFeatureController {

    private final FeatureService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureResponse create(@RequestBody @Valid CreateFeatureRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid UpdateFeatureRequest request) {
        service.update(id, request);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String id) {
        service.delete(id);
    }
}
