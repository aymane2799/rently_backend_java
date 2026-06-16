package com.rently.rently.dashboard;

import com.rently.rently.catalog.PublicCatalogService;
import com.rently.rently.catalog.VehicleCategory;
import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.fleet.vehicles.VehicleStatus;
import com.rently.rently.location.hub.HubRepository;
import com.rently.rently.reservation.payment.DepositStatus;
import com.rently.rently.reservation.payment.PaymentRepository;
import com.rently.rently.reservation.reservation.Reservation;
import com.rently.rently.reservation.reservation.ReservationRepository;
import com.rently.rently.reservation.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImplementation implements DashboardService {

    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final HubRepository hubRepository;
    private final PublicCatalogService publicCatalogService;

    @Override
    public DashboardResponse getDashboard(String branchId) {
        List<String> hubIds = branchId != null ? hubRepository.findIdsByBranchId(branchId) : null;

        DashboardResponse.FleetStatusCounts fleetStatus = computeFleetStatus(hubIds);
        DashboardResponse.ReservationCounts reservationCounts = computeReservationCounts(branchId);
        DashboardResponse.RevenueTotals revenue = computeRevenue(branchId);
        List<DashboardResponse.CategoryCount> categoryBreakdown = computeCategoryBreakdown();
        String mostRentedCategory = computeMostRentedCategory(categoryBreakdown);
        double utilisationRate = computeUtilisationRate(branchId, fleetStatus.total());
        DashboardResponse.DepositSummary depositSummary = computeDepositSummary();

        return new DashboardResponse(
                fleetStatus,
                reservationCounts,
                revenue,
                categoryBreakdown,
                mostRentedCategory,
                utilisationRate,
                depositSummary,
                Instant.now()
        );
    }

    private DashboardResponse.FleetStatusCounts computeFleetStatus(List<String> hubIds) {
        if (hubIds != null && hubIds.isEmpty()) {
            return new DashboardResponse.FleetStatusCounts(0, 0, 0, 0, 0);
        }
        long available, rented, maintenance, pendingRelocation, total;
        if (hubIds != null) {
            available = vehicleRepository.countByStatusAndCurrentHubIdIn(VehicleStatus.AVAILABLE, hubIds);
            rented = vehicleRepository.countByStatusAndCurrentHubIdIn(VehicleStatus.RENTED, hubIds);
            maintenance = vehicleRepository.countByStatusAndCurrentHubIdIn(VehicleStatus.MAINTENANCE, hubIds);
            pendingRelocation = vehicleRepository.countByStatusAndCurrentHubIdIn(VehicleStatus.PENDING_RELOCATION, hubIds);
            total = vehicleRepository.countByCurrentHubIdIn(hubIds);
        } else {
            available = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
            rented = vehicleRepository.countByStatus(VehicleStatus.RENTED);
            maintenance = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE);
            pendingRelocation = vehicleRepository.countByStatus(VehicleStatus.PENDING_RELOCATION);
            total = vehicleRepository.count();
        }
        return new DashboardResponse.FleetStatusCounts(available, rented, maintenance, pendingRelocation, total);
    }

    private DashboardResponse.ReservationCounts computeReservationCounts(String branchId) {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay();
        LocalDateTime yearStart = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.of(today.getYear() + 1, 1, 1).atStartOfDay();
        LocalDateTime priorMonthStart = monthStart.minusMonths(1);
        LocalDateTime priorMonthEnd = monthStart;
        LocalDateTime priorYearStart = yearStart.minusYears(1);
        LocalDateTime priorYearEnd = yearStart;

        List<ReservationStatus> counted = List.of(ReservationStatus.ACTIVE, ReservationStatus.CLOSED);

        if (branchId != null) {
            return new DashboardResponse.ReservationCounts(
                    reservationRepository.countByStatusInAndStartDateBetweenAndBranchId(counted, monthStart, monthEnd, branchId),
                    reservationRepository.countByStatusInAndStartDateBetweenAndBranchId(counted, yearStart, yearEnd, branchId),
                    reservationRepository.countByStatusInAndStartDateBetweenAndBranchId(counted, priorMonthStart, priorMonthEnd, branchId),
                    reservationRepository.countByStatusInAndStartDateBetweenAndBranchId(counted, priorYearStart, priorYearEnd, branchId)
            );
        }
        return new DashboardResponse.ReservationCounts(
                reservationRepository.countByStatusInAndStartDateBetween(counted, monthStart, monthEnd),
                reservationRepository.countByStatusInAndStartDateBetween(counted, yearStart, yearEnd),
                reservationRepository.countByStatusInAndStartDateBetween(counted, priorMonthStart, priorMonthEnd),
                reservationRepository.countByStatusInAndStartDateBetween(counted, priorYearStart, priorYearEnd)
        );
    }

    private DashboardResponse.RevenueTotals computeRevenue(String branchId) {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay();
        LocalDateTime yearStart = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.of(today.getYear() + 1, 1, 1).atStartOfDay();

        BigDecimal currentMonthMad, currentYearMad;
        if (branchId != null) {
            currentMonthMad = paymentRepository.sumRevenueForClosedReservationsByBranch(ReservationStatus.CLOSED, monthStart, monthEnd, branchId);
            currentYearMad = paymentRepository.sumRevenueForClosedReservationsByBranch(ReservationStatus.CLOSED, yearStart, yearEnd, branchId);
        } else {
            currentMonthMad = paymentRepository.sumRevenueForClosedReservations(ReservationStatus.CLOSED, monthStart, monthEnd);
            currentYearMad = paymentRepository.sumRevenueForClosedReservations(ReservationStatus.CLOSED, yearStart, yearEnd);
        }
        return new DashboardResponse.RevenueTotals(
                currentMonthMad != null ? currentMonthMad : BigDecimal.ZERO,
                currentYearMad != null ? currentYearMad : BigDecimal.ZERO
        );
    }

    private List<DashboardResponse.CategoryCount> computeCategoryBreakdown() {
        LocalDate today = LocalDate.now();
        LocalDateTime yearStart = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.of(today.getYear() + 1, 1, 1).atStartOfDay();

        Map<String, VehicleCategory> modelCategoryCache = new HashMap<>();

        // Fleet count by category
        Map<VehicleCategory, Long> fleetByCategory = new EnumMap<>(VehicleCategory.class);
        for (Vehicle v : vehicleRepository.findAll()) {
            VehicleCategory cat = modelCategoryCache.computeIfAbsent(
                    v.getModelId(), id -> publicCatalogService.getModel(id).getCategory());
            fleetByCategory.merge(cat, 1L, Long::sum);
        }

        // Closed reservation count by category for current year
        Map<VehicleCategory, Long> reservationsByCategory = new EnumMap<>(VehicleCategory.class);
        for (String modelId : reservationRepository.findModelIdsByStatusAndStartDateBetween(ReservationStatus.CLOSED, yearStart, yearEnd)) {
            VehicleCategory cat = modelCategoryCache.computeIfAbsent(
                    modelId, id -> publicCatalogService.getModel(id).getCategory());
            reservationsByCategory.merge(cat, 1L, Long::sum);
        }

        return Arrays.stream(VehicleCategory.values())
                .filter(cat -> fleetByCategory.containsKey(cat) || reservationsByCategory.containsKey(cat))
                .map(cat -> new DashboardResponse.CategoryCount(
                        cat.name(),
                        fleetByCategory.getOrDefault(cat, 0L),
                        reservationsByCategory.getOrDefault(cat, 0L)
                ))
                .toList();
    }

    private String computeMostRentedCategory(List<DashboardResponse.CategoryCount> breakdown) {
        return breakdown.stream()
                .filter(c -> c.closedReservationCount() > 0)
                .max(Comparator.comparingLong(DashboardResponse.CategoryCount::closedReservationCount))
                .map(DashboardResponse.CategoryCount::category)
                .orElse(null);
    }

    private double computeUtilisationRate(String branchId, long totalVehicles) {
        if (totalVehicles == 0) return 0.0;

        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay();
        long daysInMonth = today.lengthOfMonth();

        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.ACTIVE, ReservationStatus.CLOSED);
        List<Reservation> overlapping = branchId != null
                ? reservationRepository.findOverlappingByStatusesAndBranchId(activeStatuses, monthStart, monthEnd, branchId)
                : reservationRepository.findOverlappingByStatuses(activeStatuses, monthStart, monthEnd);

        long totalRentedDays = 0;
        for (Reservation r : overlapping) {
            LocalDateTime effectiveStart = r.getStartDate().isBefore(monthStart) ? monthStart : r.getStartDate();
            LocalDateTime effectiveEnd = r.getEndDate().isAfter(monthEnd) ? monthEnd : r.getEndDate();
            long days = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd);
            if (days > 0) totalRentedDays += days;
        }

        double rate = (double) totalRentedDays / (totalVehicles * daysInMonth) * 100;
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private DashboardResponse.DepositSummary computeDepositSummary() {
        LocalDate today = LocalDate.now();
        Instant monthStart = today.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant monthEnd = today.with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay().toInstant(ZoneOffset.UTC);

        long activeHoldCount = paymentRepository.countActiveHoldDeposits(DepositStatus.ACTIVE_HOLD, ReservationStatus.CANCELLED);
        BigDecimal activeHoldTotal = paymentRepository.sumActiveHoldDepositAmount(DepositStatus.ACTIVE_HOLD, ReservationStatus.CANCELLED);
        long releasedThisMonth = paymentRepository.countReleasedDepositsInPeriod(DepositStatus.RELEASED, monthStart, monthEnd);

        return new DashboardResponse.DepositSummary(
                activeHoldCount,
                activeHoldTotal != null ? activeHoldTotal : BigDecimal.ZERO,
                releasedThisMonth
        );
    }
}
