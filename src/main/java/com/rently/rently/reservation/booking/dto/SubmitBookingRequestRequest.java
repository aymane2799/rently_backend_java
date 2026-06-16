package com.rently.rently.reservation.booking.dto;

import com.rently.rently.validation.ValidEnum;
import com.rently.rently.reservation.customer.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmitBookingRequestRequest {

    @NotBlank
    private String vehicleId;

    @NotBlank
    private String pickupHubId;

    @NotBlank
    private String returnHubId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @NotBlank
    @ValidEnum(enumClass = IdType.class)
    private String idType;

    @NotBlank
    private String idNumber;

    @NotBlank
    private String driverLicenseCode;

    private String notes;
}
