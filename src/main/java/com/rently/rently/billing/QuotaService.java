package com.rently.rently.billing;

import com.rently.rently.agency.Agency;
import com.rently.rently.agency.AgencyRepository;
import com.rently.rently.auth.User;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.location.branch.BranchRepository;
import com.rently.rently.location.hub.HubRepository;
import com.rently.rently.shared.QuotaExceededException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final AgencyRepository agencyRepository;
    private final SubscriptionPlanLoader planLoader;
    private final BranchRepository branchRepository;
    private final HubRepository hubRepository;
    private final VehicleRepository vehicleRepository;

    public void assertCanAddBranch() {
        SubscriptionPlan plan = resolvePlan();
        if (plan == null || plan.getMaxBranches() == null) return;
        long count = branchRepository.countByIsActive(true);
        if (count >= plan.getMaxBranches()) {
            throw new QuotaExceededException(
                    "Branch limit reached for plan '" + plan.getDisplayName() + "' (" + plan.getMaxBranches() + " max)");
        }
    }

    public void assertCanAddHub() {
        SubscriptionPlan plan = resolvePlan();
        if (plan == null || plan.getMaxHubs() == null) return;
        long count = hubRepository.countByIsActive(true);
        if (count >= plan.getMaxHubs()) {
            throw new QuotaExceededException(
                    "Hub limit reached for plan '" + plan.getDisplayName() + "' (" + plan.getMaxHubs() + " max)");
        }
    }

    public void assertCanAddVehicle() {
        SubscriptionPlan plan = resolvePlan();
        if (plan == null || plan.getMaxVehicles() == null) return;
        long count = vehicleRepository.count();
        if (count >= plan.getMaxVehicles()) {
            throw new QuotaExceededException(
                    "Vehicle limit reached for plan '" + plan.getDisplayName() + "' (" + plan.getMaxVehicles() + " max)");
        }
    }

    private SubscriptionPlan resolvePlan() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User user)) return null;
        String agencySlug = user.getAgencySlug();
        if (agencySlug == null) return null;
        Agency agency = agencyRepository.findBySlug(agencySlug)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencySlug));
        String planId = agency.getPlanId();
        if (planId == null) return null;
        return planLoader.load(planId);
    }
}
