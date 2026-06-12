package com.rently.rently.reservation.reservation;

import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.location.hub.Hub;
import com.rently.rently.reservation.customer.Customer;
import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_hub_id", nullable = false)
    private Hub pickupHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_hub_id", nullable = false)
    private Hub returnHub;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "is_digitally_signed", nullable = false)
    private boolean isDigitallySigned = false;

    @Builder.Default
    @Column(name = "is_physically_printed", nullable = false)
    private boolean isPhysicallyPrinted = false;

    @Column(name = "signature_base64", columnDefinition = "TEXT")
    private String signatureBase64;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", nullable = false)
    private ContractStatus contractStatus = ContractStatus.PENDING;

    @Column(name = "created_by", nullable = false)
    private String createdBy;
}
