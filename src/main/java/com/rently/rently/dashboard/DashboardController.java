package com.rently.rently.dashboard;

import com.rently.rently.auth.User;
import com.rently.rently.auth.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER', 'AGENT')")
    public DashboardResponse getDashboard(@AuthenticationPrincipal User user) {
        String branchId = user.getRole() == UserRole.AGENCY_OWNER ? null : user.getBranchId();
        return dashboardService.getDashboard(branchId);
    }
}
