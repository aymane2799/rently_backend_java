package com.rently.rently.reservation.reservation;

import com.rently.rently.fleet.vehicles.VehicleResponse;
import com.rently.rently.location.hub.HubResponse;
import com.rently.rently.reservation.customer.CustomerResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationResponse {
    private String id;
    private CustomerResponse customer;
    private VehicleResponse vehicle;
    private HubResponse pickupHub;
    private HubResponse returnHub;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ReservationStatus status;
    private BigDecimal totalAmount;
    private boolean isDigitallySigned;
    private boolean isPhysicallyPrinted;
    private String signatureBase64;
    private ContractStatus contractStatus;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}
