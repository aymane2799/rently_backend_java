package com.rently.rently.catalog.brands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/car-brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandServiceImplementation service;

    @GetMapping
    public List<BrandResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public BrandResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@RequestBody @Valid CreateBrandRequest request){
        return service.create(request);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid UpdateBrandRequest request){
        service.update(id, request);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id){
        service.delete(id);
    }
}
