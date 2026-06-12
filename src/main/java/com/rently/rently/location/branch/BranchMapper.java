package com.rently.rently.location.branch;

import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper implements
        ResponseMapper<Branch, BranchResponse>,
        CreateMapper<Branch, CreateBranchRequest, Object>,
        PatchMapper<Branch, UpdateBranchRequest, Object> {

    @Override
    public Branch toEntity(CreateBranchRequest request, Object ctx) {
        return Branch.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();
    }

    @Override
    public void patchEntity(Branch entity, UpdateBranchRequest request, Object ctx) {
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCity() != null) entity.setCity(request.getCity());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
    }

    @Override
    public BranchResponse toResponse(Branch entity) {
        return BranchResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .city(entity.getCity())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
