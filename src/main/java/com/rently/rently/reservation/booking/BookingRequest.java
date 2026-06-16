package com.rently.rently.reservation.booking;

import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.location.hub.Hub;
import com.rently.rently.reservation.client.ClientAccount;
import com.rently.rently.reservation.customer.IdType;
import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientAccount clientAccount;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IdType idType;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "driver_license_code", nullable = false)
    private String driverLicenseCode;

    @Column(name = "id_document_url", nullable = false)
    private String idDocumentUrl;

    @Column(name = "driver_license_document_url", nullable = false)
    private String driverLicenseDocumentUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingRequestStatus status = BookingRequestStatus.PENDING_CONFIRMATION;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "confirmed_by")
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "converted_reservation_id")
    private String convertedReservationId;
}
