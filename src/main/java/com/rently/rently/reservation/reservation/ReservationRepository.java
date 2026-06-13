package com.rently.rently.reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
}
