package com.rently.rently.reservation.booking.dto;

import com.rently.rently.reservation.booking.BookingRequestStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequestResponse {
    private String id;
    private String clientId;
    private String vehicleId;
    private String vehicleLicensePlate;
    private String pickupHubId;
    private String pickupHubName;
    private String returnHubId;
    private String returnHubName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String idType;
    private String idNumber;
    private String driverLicenseCode;
    private String idDocumentUrl;
    private String driverLicenseDocumentUrl;
    private BookingRequestStatus status;
    private String notes;
    private String rejectionReason;
    private String confirmedBy;
    private Instant confirmedAt;
    private String rejectedBy;
    private Instant rejectedAt;
    private String convertedReservationId;
    private Instant createdAt;
    private Instant updatedAt;
}
