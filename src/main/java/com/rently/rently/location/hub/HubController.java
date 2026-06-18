package com.rently.rently.location.hub;

import com.rently.rently.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HubController {

    private final HubService service;

    @GetMapping("/api/v1/branches/{branchId}/hubs")
    public PagedResponse<HubResponse> getAllByBranch(
            @PathVariable String branchId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) HubType type,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAllByBranch(branchId, active, type, pageable);
    }

    @GetMapping("/api/v1/hubs/options")
    public List<HubOptionResponse> getOptions(
            @RequestParam(required = true) String branchId
    ) {
        return service.getOptions(branchId);
    }

    @PostMapping("/api/v1/branches/{branchId}/hubs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public HubResponse create(@PathVariable String branchId, @RequestBody @Valid CreateHubRequest request) {
        return service.create(branchId, request);
    }

    @PatchMapping("/api/v1/hubs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public void update(@PathVariable String id, @RequestBody @Valid UpdateHubRequest request) {
        service.update(id, request);
    }

    @PostMapping("/api/v1/hubs/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public void deactivate(@PathVariable String id) {
        service.deactivate(id);
    }
}
