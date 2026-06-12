package com.rently.rently.reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReservationRequest {

    @NotBlank
    private String customerId;

    @NotBlank
    private String vehicleId;

    @NotBlank
    private String pickupHubId;

    @NotBlank
    private String returnHubId;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    @NotNull
    @Positive
    private BigDecimal totalAmount;
}
