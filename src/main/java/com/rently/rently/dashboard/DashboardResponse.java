package com.rently.rently.dashboard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardResponse(
        FleetStatusCounts fleetStatus,
        ReservationCounts reservationCounts,
        RevenueTotals revenue,
        List<CategoryCount> categoryBreakdown,
        String mostRentedCategory,
        double utilisationRate,
        DepositSummary depositSummary,
        Instant generatedAt
) {

    public record FleetStatusCounts(
            long available,
            long rented,
            long maintenance,
            long pendingRelocation,
            long total
    ) {}

    public record ReservationCounts(
            long currentMonth,
            long currentYear,
            long priorMonth,
            long priorYear
    ) {}

    public record RevenueTotals(
            BigDecimal currentMonthMad,
            BigDecimal currentYearMad
    ) {}

    public record CategoryCount(
            String category,
            long fleetCount,
            long closedReservationCount
    ) {}

    public record DepositSummary(
            long activeHoldCount,
            BigDecimal activeHoldTotalMad,
            long releasedThisMonthCount
    ) {}
}
