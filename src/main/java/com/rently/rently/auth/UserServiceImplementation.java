package com.rently.rently.auth;

import com.rently.rently.auth.dto.RegisterUserRequest;
import com.rently.rently.auth.dto.UpdateUserRequest;
import com.rently.rently.auth.dto.UserResponse;
import com.rently.rently.shared.PagedResponse;
import com.rently.rently.shared.PagedResponseMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> getAll(String agencySlug) {
        return repository.findAllByAgencySlug(agencySlug)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public PagedResponse<UserResponse> getAll(String agencySlug, UserRole role, String branchId, Boolean active, String search, Pageable pageable) {
        Specification<User> spec = UserSpecification.withFilters(agencySlug, role, branchId, active, search);
        return PagedResponseMapper.toPagedResponse(repository.findAll(spec, pageable), userMapper::toResponse);
    }

    @Override
    public UserResponse get(String id) {
        return userMapper.toResponse(findById(id));
    }

    @Override
    public UserResponse create(RegisterUserRequest request, String agencySlug) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException("User with email " + request.getEmail() + " already exists!");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .agencySlug(agencySlug)
                .branchId(request.getBranchId())
                .build();
        return userMapper.toResponse(repository.save(user));
    }

    @Override
    public void update(String id, UpdateUserRequest request) {
        User user = findById(id);
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPassword() != null) user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (request.getBranchId() != null) user.setBranchId(request.getBranchId());
        if (request.getActive() != null) user.setActive(request.getActive());
        repository.save(user);
    }

    @Override
    public void delete(String id) {
        repository.delete(findById(id));
    }

    private User findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found!"));
    }

}
