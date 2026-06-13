package com.rently.rently.reservation.payment;

import com.rently.rently.reservation.reservation.Reservation;
import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends Auditable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "total_contract_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalContractAmount;

    @Builder.Default
    @Column(name = "cash_advanced", nullable = false, precision = 10, scale = 2)
    private BigDecimal cashAdvanced = BigDecimal.ZERO;

    @Column(name = "bank_transfer_reference")
    private String bankTransferReference;

    @Column(name = "bank_transfer_image_url")
    private String bankTransferImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_type", nullable = false)
    private DepositType depositType;

    @Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_status", nullable = false)
    private DepositStatus depositStatus = DepositStatus.ACTIVE_HOLD;

    @Column(name = "cheque_number")
    private String chequeNumber;

    @Column(name = "credit_card_auth_reference")
    private String creditCardAuthReference;

    @Column(name = "deposit_released_at")
    private Instant depositReleasedAt;

    @Column(name = "deposit_released_by")
    private String depositReleasedBy;
}
