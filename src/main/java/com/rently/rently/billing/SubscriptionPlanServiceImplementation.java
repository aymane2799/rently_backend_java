package com.rently.rently.billing;

import com.rently.rently.billing.dto.CreateSubscriptionPlanRequest;
import com.rently.rently.billing.dto.SubscriptionPlanResponse;
import com.rently.rently.billing.dto.UpdateSubscriptionPlanRequest;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImplementation implements SubscriptionPlanService {

    private final SubscriptionPlanRepository repository;
    private final SubscriptionPlanMapper mapper;

    @Override
    public List<SubscriptionPlanResponse> getActivePlans() {
        return repository.findAllByActiveTrue().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<SubscriptionPlanResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public SubscriptionPlanResponse get(String id) {
        return mapper.toResponse(findById(id));
    }

    @Override
    public SubscriptionPlanResponse create(CreateSubscriptionPlanRequest request) {
        if (repository.existsByCode(request.getCode())) {
            throw new EntityExistsException("Subscription plan with code " + request.getCode() + " already exists");
        }
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .code(request.getCode())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .priceMonthly(request.getPriceMonthly())
                .priceYearly(request.getPriceYearly())
                .maxBranches(request.getMaxBranches())
                .maxHubs(request.getMaxHubs())
                .maxVehicles(request.getMaxVehicles())
                .build();
        return mapper.toResponse(repository.save(plan));
    }

    @Override
    public SubscriptionPlanResponse update(String id, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = findById(id);
        if (request.getDisplayName() != null) plan.setDisplayName(request.getDisplayName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getPriceMonthly() != null) plan.setPriceMonthly(request.getPriceMonthly());
        if (request.getPriceYearly() != null) plan.setPriceYearly(request.getPriceYearly());
        if (request.getMaxBranches() != null) plan.setMaxBranches(request.getMaxBranches());
        if (request.getMaxHubs() != null) plan.setMaxHubs(request.getMaxHubs());
        if (request.getMaxVehicles() != null) plan.setMaxVehicles(request.getMaxVehicles());
        return mapper.toResponse(repository.save(plan));
    }

    @Override
    public void deactivate(String id) {
        SubscriptionPlan plan = findById(id);
        plan.setActive(false);
        repository.save(plan);
    }

    private SubscriptionPlan findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan with id " + id + " not found"));
    }
}
