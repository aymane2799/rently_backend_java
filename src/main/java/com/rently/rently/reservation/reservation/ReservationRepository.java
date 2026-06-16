package com.rently.rently.reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.customer JOIN FETCH r.vehicle JOIN FETCH r.pickupHub JOIN FETCH r.returnHub WHERE r.id = :id")
    Optional<Reservation> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT r FROM Reservation r WHERE r.vehicle.id = :vehicleId AND r.status = :status AND r.startDate < :endDate AND r.endDate > :startDate")
    List<Reservation> findOverlapping(
            @Param("vehicleId") String vehicleId,
            @Param("status") ReservationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT DISTINCT r.vehicle.id FROM Reservation r WHERE r.status = :status AND r.startDate < :to AND r.endDate > :from")
    List<String> findVehicleIdsWithOverlappingReservations(
            @Param("status") ReservationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status IN :statuses AND r.startDate >= :from AND r.startDate < :to")
    long countByStatusInAndStartDateBetween(
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status IN :statuses AND r.startDate >= :from AND r.startDate < :to AND r.pickupHub.branch.id = :branchId")
    long countByStatusInAndStartDateBetweenAndBranchId(
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("branchId") String branchId
    );

    @Query("SELECT r FROM Reservation r JOIN FETCH r.vehicle WHERE r.status IN :statuses AND r.startDate < :to AND r.endDate > :from")
    List<Reservation> findOverlappingByStatuses(
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT r FROM Reservation r JOIN FETCH r.vehicle WHERE r.status IN :statuses AND r.startDate < :to AND r.endDate > :from AND r.pickupHub.branch.id = :branchId")
    List<Reservation> findOverlappingByStatusesAndBranchId(
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("branchId") String branchId
    );

    @Query("SELECT r.vehicle.modelId FROM Reservation r WHERE r.status = :status AND r.startDate >= :from AND r.startDate < :to")
    List<String> findModelIdsByStatusAndStartDateBetween(
            @Param("status") ReservationStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
