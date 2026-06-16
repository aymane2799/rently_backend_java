package com.rently.rently.reservation.booking;

import com.rently.rently.reservation.booking.dto.BookingRequestResponse;
import org.springframework.stereotype.Component;

@Component
public class BookingRequestMapper {

    public BookingRequestResponse toResponse(BookingRequest request) {
        return BookingRequestResponse.builder()
                .id(request.getId())
                .clientId(request.getClientAccount().getId())
                .vehicleId(request.getVehicle().getId())
                .vehicleLicensePlate(request.getVehicle().getLicensePlate())
                .pickupHubId(request.getPickupHub().getId())
                .pickupHubName(request.getPickupHub().getName())
                .returnHubId(request.getReturnHub().getId())
                .returnHubName(request.getReturnHub().getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .idType(request.getIdType().name())
                .idNumber(request.getIdNumber())
                .driverLicenseCode(request.getDriverLicenseCode())
                .idDocumentUrl(request.getIdDocumentUrl())
                .driverLicenseDocumentUrl(request.getDriverLicenseDocumentUrl())
                .status(request.getStatus())
                .notes(request.getNotes())
                .rejectionReason(request.getRejectionReason())
                .confirmedBy(request.getConfirmedBy())
                .confirmedAt(request.getConfirmedAt())
                .rejectedBy(request.getRejectedBy())
                .rejectedAt(request.getRejectedAt())
                .convertedReservationId(request.getConvertedReservationId())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
