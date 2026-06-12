package com.rently.rently.auth;

import com.rently.rently.auth.dto.RegisterUserRequest;
import com.rently.rently.auth.dto.UpdateUserRequest;
import com.rently.rently.auth.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    @PreAuthorize("hasRole('AGENCY_OWNER')")
    public List<UserResponse> getAll(@AuthenticationPrincipal User currentUser) {
        return service.getAll(currentUser.getAgencySlug());
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
