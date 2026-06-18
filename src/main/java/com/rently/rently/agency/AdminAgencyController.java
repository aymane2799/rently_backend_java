package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyResponse;
import com.rently.rently.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final AgencyService service;

    @GetMapping
    public PagedResponse<AgencyResponse> getAll(
            @RequestParam(required = false) AgencyStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAll(status, city, planId, search, pageable);
    }

    @GetMapping("/{id}")
    public AgencyResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/{id}/block")
    public AgencyResponse block(@PathVariable String id) {
        return service.block(id);
    }

    @PostMapping("/{id}/unblock")
    public AgencyResponse unblock(@PathVariable String id) {
        return service.unblock(id);
    }
}
