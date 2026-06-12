package com.rently.rently.reservation.reservation;

import java.util.List;

public interface ReservationService {
    ReservationResponse create(CreateReservationRequest request, String createdBy);
    List<ReservationResponse> getAll();
    ReservationResponse get(String id);
    void close(String id);
    void cancel(String id);
    void updateContractStatus(String id);
}
