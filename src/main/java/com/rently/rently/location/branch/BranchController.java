package com.rently.rently.location.branch;

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
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService service;

    @GetMapping
    public PagedResponse<BranchResponse> getAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAll(active, city, search, pageable);
    }

    @GetMapping("/options")
    public List<BranchOptionResponse> getOptions() {
        return service.getOptions();
    }

    @GetMapping("/{id}")
    public BranchResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public BranchResponse create(@RequestBody @Valid CreateBranchRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER')")
    public void update(@PathVariable String id, @RequestBody @Valid UpdateBranchRequest request) {
        service.update(id, request);
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public void deactivate(@PathVariable String id) {
        service.delete(id);
    }
}
