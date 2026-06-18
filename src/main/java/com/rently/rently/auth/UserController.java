package com.rently.rently.auth;

import com.rently.rently.auth.dto.RegisterUserRequest;
import com.rently.rently.auth.dto.UpdateUserRequest;
import com.rently.rently.auth.dto.UserResponse;
import com.rently.rently.shared.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public PagedResponse<UserResponse> getAll(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.getAll(currentUser.getAgencySlug(), role, branchId, active, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public UserResponse create(@RequestBody @Valid RegisterUserRequest request,
                               @AuthenticationPrincipal User currentUser) {
        return service.create(request, currentUser.getAgencySlug());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public void update(@PathVariable String id, @RequestBody @Valid UpdateUserRequest request) {
        service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
