package com.rently.rently.location.hub;

import com.rently.rently.location.branch.BranchMapper;
import com.rently.rently.location.hub.hydration.HubHydrationContext;
import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubMapper implements
        ResponseMapper<Hub, HubResponse>,
        CreateMapper<Hub, CreateHubRequest, HubHydrationContext>,
        PatchMapper<Hub, UpdateHubRequest, HubHydrationContext> {

    private final BranchMapper branchMapper;

    @Override
    public Hub toEntity(CreateHubRequest request, HubHydrationContext ctx) {
        return Hub.builder()
                .name(request.getName())
                .type(request.getType())
                .city(request.getCity())
                .address(request.getAddress())
                .branch(ctx.getBranch())
                .build();
    }

    @Override
    public void patchEntity(Hub entity, UpdateHubRequest request, HubHydrationContext ctx) {
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getType() != null) entity.setType(request.getType());
        if (request.getCity() != null) entity.setCity(request.getCity());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
    }

    @Override
    public HubResponse toResponse(Hub entity) {
        return HubResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .city(entity.getCity())
                .address(entity.getAddress())
                .isActive(entity.isActive())
                .branch(branchMapper.toResponse(entity.getBranch()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
