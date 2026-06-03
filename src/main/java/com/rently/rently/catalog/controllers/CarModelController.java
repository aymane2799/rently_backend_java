package com.rently.rently.catalog.controllers;


import com.rently.rently.catalog.reponses.CarModelResponse;
import com.rently.rently.catalog.requests.CarModelRequest;
import com.rently.rently.catalog.services_implementation.CarModelServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/car_models")
@RequiredArgsConstructor
public class CarModelController {
    private final CarModelServiceImplementation service;

    @GetMapping
    public List<CarModelResponse> getAll() {
        return this.service.getAll();
    }

    @GetMapping("{id}")
    public CarModelResponse get(@PathVariable String id) {
        return this.service.get(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CarModelResponse create(@RequestBody @Valid CarModelRequest request){
        return this.service.create(request);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid CarModelRequest request){
        this.service.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id){
        this.service.delete(id);
    }
}
