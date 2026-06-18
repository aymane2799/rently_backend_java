package com.rently.rently.reservation.reservation;

import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    ReservationResponse create(CreateReservationRequest request, String createdBy);
    List<ReservationResponse> getAll();
    PagedResponse<ReservationResponse> getAll(ReservationStatus status, ContractStatus contractStatus, String customerId, String vehicleId, LocalDateTime startDateFrom, LocalDateTime startDateTo, Pageable pageable);
    ReservationResponse get(String id);
    void close(String id);
    void cancel(String id);
    void updateContractStatus(String id);
    void sign(String id, String signatureBase64);
    void markPrinted(String id);
}
