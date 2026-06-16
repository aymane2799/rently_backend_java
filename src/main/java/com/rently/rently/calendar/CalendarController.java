package com.rently.rently.calendar;

import com.rently.rently.auth.User;
import com.rently.rently.auth.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('AGENCY_OWNER', 'BRANCH_MANAGER', 'AGENT')")
    public List<CalendarEventResponse> getEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal User user) {
        String branchId = user.getRole() == UserRole.AGENCY_OWNER ? null : user.getBranchId();
        return calendarService.getEvents(from, to, branchId);
    }
}
