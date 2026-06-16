package com.rently.rently.reservation.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RejectBookingRequestRequest {

    @NotBlank
    private String rejectionReason;
}
