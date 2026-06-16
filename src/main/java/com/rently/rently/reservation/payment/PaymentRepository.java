package com.rently.rently.reservation.payment;

import com.rently.rently.reservation.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByReservationId(String reservationId);

    @Query("SELECT COALESCE(SUM(p.totalContractAmount), 0) FROM Payment p WHERE p.reservation.status = :status AND p.reservation.startDate >= :from AND p.reservation.startDate < :to")
    BigDecimal sumRevenueForClosedReservations(
            @Param("status") ReservationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT COALESCE(SUM(p.totalContractAmount), 0) FROM Payment p WHERE p.reservation.status = :status AND p.reservation.startDate >= :from AND p.reservation.startDate < :to AND p.reservation.pickupHub.branch.id = :branchId")
    BigDecimal sumRevenueForClosedReservationsByBranch(
            @Param("status") ReservationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("branchId") String branchId
    );

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.depositStatus = :depositStatus AND p.reservation.status <> :excludeStatus")
    long countActiveHoldDeposits(
            @Param("depositStatus") DepositStatus depositStatus,
            @Param("excludeStatus") ReservationStatus excludeStatus
    );

    @Query("SELECT COALESCE(SUM(p.depositAmount), 0) FROM Payment p WHERE p.depositStatus = :depositStatus AND p.reservation.status <> :excludeStatus")
    BigDecimal sumActiveHoldDepositAmount(
            @Param("depositStatus") DepositStatus depositStatus,
            @Param("excludeStatus") ReservationStatus excludeStatus
    );

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.depositStatus = :depositStatus AND p.depositReleasedAt >= :from AND p.depositReleasedAt < :to")
    long countReleasedDepositsInPeriod(
            @Param("depositStatus") DepositStatus depositStatus,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
