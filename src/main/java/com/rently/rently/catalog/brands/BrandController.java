package com.rently.rently.catalog.brands;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;

    @GetMapping
    public List<BrandResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public BrandResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
