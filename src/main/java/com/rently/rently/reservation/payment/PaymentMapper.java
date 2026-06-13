package com.rently.rently.reservation.payment;

import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .reservationId(payment.getReservation().getId())
                .totalContractAmount(payment.getTotalContractAmount())
                .cashAdvanced(payment.getCashAdvanced())
                .bankTransferReference(payment.getBankTransferReference())
                .bankTransferImageUrl(payment.getBankTransferImageUrl())
                .depositType(payment.getDepositType())
                .depositAmount(payment.getDepositAmount())
                .depositStatus(payment.getDepositStatus())
                .chequeNumber(payment.getChequeNumber())
                .creditCardAuthReference(payment.getCreditCardAuthReference())
                .depositReleasedAt(payment.getDepositReleasedAt())
                .depositReleasedBy(payment.getDepositReleasedBy())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
