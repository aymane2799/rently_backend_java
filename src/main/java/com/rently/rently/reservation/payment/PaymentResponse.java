package com.rently.rently.reservation.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {
    private String id;
    private String reservationId;
    private BigDecimal totalContractAmount;
    private BigDecimal cashAdvanced;
    private String bankTransferReference;
    private String bankTransferImageUrl;
    private DepositType depositType;
    private BigDecimal depositAmount;
    private DepositStatus depositStatus;
    private String chequeNumber;
    private String creditCardAuthReference;
    private Instant depositReleasedAt;
    private String depositReleasedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
