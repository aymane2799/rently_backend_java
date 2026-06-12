package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final AgencyService service;

    @GetMapping
    public List<AgencyResponse> getAll(@RequestParam(required = false) AgencyStatus status) {
        return service.getAll(status);
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
