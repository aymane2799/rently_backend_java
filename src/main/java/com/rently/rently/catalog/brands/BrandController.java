package com.rently.rently.catalog.brands;

import com.rently.rently.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;

    @GetMapping
    public PagedResponse<BrandResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAll(search, isActive, pageable);
    }

    @GetMapping("/options")
    public List<BrandOptionResponse> getOptions() {
        return service.getOptions();
    }

    @GetMapping("/{id}")
    public BrandResponse get(@PathVariable String id) {
        return service.get(id);
    }
}
