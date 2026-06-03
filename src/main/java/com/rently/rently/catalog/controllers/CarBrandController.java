package com.rently.rently.catalog.controllers;

import com.rently.rently.catalog.reponses.CarBrandResponse;
import com.rently.rently.catalog.requests.CarBrandRequest;
import com.rently.rently.catalog.services_implementation.CarBrandServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/car_brands")
@RequiredArgsConstructor
public class CarBrandController {
    private final CarBrandServiceImplementation service;

    @GetMapping
    public List<CarBrandResponse> getAll() {
        return this.service.getAll();
    }

    @GetMapping("{id}")
    public CarBrandResponse get(@PathVariable String id) {
        return this.service.get(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CarBrandResponse create(@RequestBody @Valid CarBrandRequest request){
        return this.service.create(request);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid CarBrandRequest request){
        this.service.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id){
        this.service.delete(id);
    }
}
