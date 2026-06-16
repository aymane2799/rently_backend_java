package com.rently.rently.reservation.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, String> {

    @Query("SELECT request FROM BookingRequest request WHERE request.vehicle.id = :vehicleId AND request.status = 'PENDING_CONFIRMATION' AND request.startDate < :endDate AND request.endDate > :startDate")
    List<BookingRequest> findOverlappingPending(
            @Param("vehicleId") String vehicleId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT request FROM BookingRequest request WHERE br.clientAccount.id = :clientId ORDER BY br.createdAt DESC")
    List<BookingRequest> findByClientId(@Param("clientId") String clientId);

    List<BookingRequest> findAllByStatus(BookingRequestStatus status);
}
