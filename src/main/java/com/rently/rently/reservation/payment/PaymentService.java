package com.rently.rently.reservation.payment;

import com.rently.rently.reservation.reservation.Reservation;

import java.util.Optional;

public interface PaymentService {
    void createForReservation(Reservation reservation, CreatePaymentRequest request);
    PaymentResponse getForReservation(String reservationId);
    Optional<PaymentResponse> findForReservation(String reservationId);
    void releaseDeposit(String reservationId, String releasedBy);
}
