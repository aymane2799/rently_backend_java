package com.rently.rently.reservation.payment;

import com.rently.rently.reservation.reservation.Reservation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImplementation implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public void createForReservation(Reservation reservation, CreatePaymentRequest request) {
        Payment payment = Payment.builder()
                .reservation(reservation)
                .totalContractAmount(reservation.getTotalAmount())
                .cashAdvanced(request.getCashAdvanced())
                .bankTransferReference(request.getBankTransferReference())
                .bankTransferImageUrl(request.getBankTransferImageUrl())
                .depositType(request.getDepositType())
                .depositAmount(request.getDepositAmount())
                .chequeNumber(request.getChequeNumber())
                .creditCardAuthReference(request.getCreditCardAuthReference())
                .build();
        repository.save(payment);
    }

    @Override
    public PaymentResponse getForReservation(String reservationId) {
        Payment payment = repository.findByReservationId(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Payment for reservation " + reservationId + " not found"));
        return mapper.toResponse(payment);
    }

    @Override
    public Optional<PaymentResponse> findForReservation(String reservationId) {
        return repository.findByReservationId(reservationId).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void releaseDeposit(String reservationId, String releasedBy) {
        Payment payment = repository.findByReservationId(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Payment for reservation " + reservationId + " not found"));

        if (payment.getDepositStatus() == DepositStatus.RELEASED) {
            throw new IllegalStateException("Deposit for reservation " + reservationId + " has already been released");
        }

        payment.setDepositStatus(DepositStatus.RELEASED);
        payment.setDepositReleasedAt(Instant.now());
        payment.setDepositReleasedBy(releasedBy);
        repository.save(payment);
    }
}
