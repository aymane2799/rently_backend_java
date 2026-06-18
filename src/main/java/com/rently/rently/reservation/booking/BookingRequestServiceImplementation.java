package com.rently.rently.reservation.booking;

import com.rently.rently.document.ContractPdfService;
import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.fleet.vehicles.VehicleStatus;
import com.rently.rently.location.hub.Hub;
import com.rently.rently.location.hub.HubRepository;
import com.rently.rently.multitenancy.TenantContext;
import com.rently.rently.reservation.booking.dto.BookingRequestResponse;
import com.rently.rently.reservation.booking.dto.SubmitBookingRequestRequest;
import com.rently.rently.reservation.client.ClientAccount;
import com.rently.rently.reservation.client.ClientAccountRepository;
import com.rently.rently.reservation.customer.Customer;
import com.rently.rently.reservation.customer.CustomerRepository;
import com.rently.rently.reservation.customer.IdType;
import com.rently.rently.reservation.payment.DepositStatus;
import com.rently.rently.reservation.payment.DepositType;
import com.rently.rently.reservation.payment.Payment;
import com.rently.rently.reservation.payment.PaymentRepository;
import com.rently.rently.reservation.reservation.Reservation;
import com.rently.rently.reservation.reservation.ReservationRepository;
import com.rently.rently.reservation.reservation.ReservationStatus;
import com.rently.rently.shared.PagedResponse;
import com.rently.rently.shared.PagedResponseMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingRequestServiceImplementation implements BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final ClientAccountRepository clientAccountRepository;
    private final VehicleRepository vehicleRepository;
    private final HubRepository hubRepository;
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRequestMapper mapper;
    private final ContractPdfService contractPdfService;

    @Value("${document.storage.path:./documents}")
    private String storagePath;

    @Override
    @Transactional
    public BookingRequestResponse submit(String clientId, SubmitBookingRequestRequest request,
                                         MultipartFile idDocumentFile, MultipartFile driverLicenseDocumentFile) {
        ClientAccount client = clientAccountRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found: " + request.getVehicleId()));

        Hub pickupHub = hubRepository.findById(request.getPickupHubId())
                .orElseThrow(() -> new EntityNotFoundException("Pickup hub not found: " + request.getPickupHubId()));

        Hub returnHub = hubRepository.findById(request.getReturnHubId())
                .orElseThrow(() -> new EntityNotFoundException("Return hub not found: " + request.getReturnHubId()));

        if (request.getStartDate().isAfter(request.getEndDate()) || request.getStartDate().isEqual(request.getEndDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        boolean hasOverlappingReservation = !reservationRepository.findOverlapping(
                request.getVehicleId(), ReservationStatus.ACTIVE,
                request.getStartDate(), request.getEndDate()).isEmpty();

        boolean hasOverlappingBooking = !bookingRequestRepository.findOverlappingPending(
                request.getVehicleId(), request.getStartDate(), request.getEndDate()).isEmpty();

        if (hasOverlappingReservation || hasOverlappingBooking) {
            throw new IllegalStateException("Vehicle is not available for the selected dates");
        }

        String idDocumentUrl = saveFile(idDocumentFile, clientId + "-id");
        String licenseDocumentUrl = saveFile(driverLicenseDocumentFile, clientId + "-license");

        BookingRequest bookingRequest = BookingRequest.builder()
                .clientAccount(client)
                .vehicle(vehicle)
                .pickupHub(pickupHub)
                .returnHub(returnHub)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .idType(IdType.valueOf(request.getIdType()))
                .idNumber(request.getIdNumber())
                .driverLicenseCode(request.getDriverLicenseCode())
                .idDocumentUrl(idDocumentUrl)
                .driverLicenseDocumentUrl(licenseDocumentUrl)
                .notes(request.getNotes())
                .build();

        return mapper.toResponse(bookingRequestRepository.save(bookingRequest));
    }

    @Override
    @Transactional
    public BookingRequestResponse confirm(String requestId, String agentUserId) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found: " + requestId));

        if (bookingRequest.getStatus() != BookingRequestStatus.PENDING_CONFIRMATION) {
            throw new IllegalStateException("Booking request is not in PENDING_CONFIRMATION status");
        }

        ClientAccount client = bookingRequest.getClientAccount();

        Customer customer = customerRepository.findByIdTypeAndIdNumber(
                        bookingRequest.getIdType(), bookingRequest.getIdNumber())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .phone(client.getPhone())
                        .email(client.getEmail())
                        .idType(bookingRequest.getIdType())
                        .idNumber(bookingRequest.getIdNumber())
                        .driverLicenseCode(bookingRequest.getDriverLicenseCode())
                        .build()));

        Vehicle vehicle = bookingRequest.getVehicle();
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle is no longer available (status: " + vehicle.getStatus() + ")");
        }

        List<Reservation> overlapping = reservationRepository.findOverlapping(
                vehicle.getId(), ReservationStatus.ACTIVE,
                bookingRequest.getStartDate(), bookingRequest.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Vehicle is no longer available for the requested dates");
        }

        long days = Math.max(1, ChronoUnit.DAYS.between(bookingRequest.getStartDate(), bookingRequest.getEndDate()));
        BigDecimal dailyRate = vehicle.getDailyBaseRate() != null ? vehicle.getDailyBaseRate() : BigDecimal.ZERO;
        BigDecimal totalAmount = dailyRate.multiply(BigDecimal.valueOf(days));

        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .vehicle(vehicle)
                .pickupHub(bookingRequest.getPickupHub())
                .returnHub(bookingRequest.getReturnHub())
                .startDate(bookingRequest.getStartDate())
                .endDate(bookingRequest.getEndDate())
                .totalAmount(totalAmount)
                .createdBy(agentUserId)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        paymentRepository.save(Payment.builder()
                .reservation(savedReservation)
                .totalContractAmount(totalAmount)
                .cashAdvanced(BigDecimal.ZERO)
                .depositType(DepositType.CASH)
                .depositAmount(BigDecimal.ZERO)
                .depositStatus(DepositStatus.ACTIVE_HOLD)
                .build());

        String tenantSlug = TenantContext.getTenantId();
        contractPdfService.generateAsync(savedReservation.getId(), tenantSlug);

        bookingRequest.setStatus(BookingRequestStatus.CONFIRMED);
        bookingRequest.setConfirmedBy(agentUserId);
        bookingRequest.setConfirmedAt(Instant.now());
        bookingRequest.setConvertedReservationId(savedReservation.getId());

        return mapper.toResponse(bookingRequestRepository.save(bookingRequest));
    }

    @Override
    @Transactional
    public void reject(String requestId, String agentUserId, String rejectionReason) {
        BookingRequest bookingRequest = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found: " + requestId));

        if (bookingRequest.getStatus() != BookingRequestStatus.PENDING_CONFIRMATION) {
            throw new IllegalStateException("Booking request is not in PENDING_CONFIRMATION status");
        }

        bookingRequest.setStatus(BookingRequestStatus.REJECTED);
        bookingRequest.setRejectedBy(agentUserId);
        bookingRequest.setRejectedAt(Instant.now());
        bookingRequest.setRejectionReason(rejectionReason);

        bookingRequestRepository.save(bookingRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingRequestResponse> getForClient(String clientId) {
        return bookingRequestRepository.findByClientId(clientId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingRequestResponse getForClient(String clientId, String requestId) {
        BookingRequest br = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found: " + requestId));
        if (!br.getClientAccount().getId().equals(clientId)) {
            throw new EntityNotFoundException("Booking request not found: " + requestId);
        }
        return mapper.toResponse(br);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingRequestResponse> getAll(BookingRequestStatus status) {
        List<BookingRequest> results = status != null
                ? bookingRequestRepository.findAllByStatus(status)
                : bookingRequestRepository.findAll();
        return results.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingRequestResponse> getAll(BookingRequestStatus status, String vehicleId, Pageable pageable) {
        Specification<BookingRequest> spec = BookingRequestSpecification.withFilters(status, vehicleId);
        return PagedResponseMapper.toPagedResponse(bookingRequestRepository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingRequestResponse get(String requestId) {
        return mapper.toResponse(bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found: " + requestId)));
    }

    private String saveFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }
        try {
            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : ".pdf";
            String filename = prefix + "-" + UUID.randomUUID() + extension;
            Path dir = Paths.get(storagePath, "booking-docs");
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.write(dest, file.getBytes());
            return dest.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document: " + e.getMessage(), e);
        }
    }
}
