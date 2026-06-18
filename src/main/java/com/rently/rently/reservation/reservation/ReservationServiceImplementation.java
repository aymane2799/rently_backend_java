package com.rently.rently.reservation.reservation;

import com.rently.rently.document.ContractPdfService;
import com.rently.rently.document.InvoicePdfService;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.fleet.vehicles.VehicleStatus;
import com.rently.rently.multitenancy.TenantContext;
import com.rently.rently.reservation.payment.PaymentResponse;
import com.rently.rently.reservation.payment.PaymentService;
import com.rently.rently.reservation.reservation.hydration.ReservationHydrationContext;
import com.rently.rently.reservation.reservation.hydration.ReservationHydrator;
import com.rently.rently.shared.PagedResponse;
import com.rently.rently.shared.PagedResponseMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImplementation implements ReservationService {

    private final ReservationRepository repository;
    private final VehicleRepository vehicleRepository;
    private final ReservationMapper mapper;
    private final ReservationHydrator hydrator;
    private final PaymentService paymentService;
    private final ContractPdfService contractPdfService;
    private final InvoicePdfService invoicePdfService;

    @Override
    @Transactional
    public ReservationResponse create(CreateReservationRequest request, String createdBy) {
        ReservationHydrationContext ctx = hydrator.hydrate(request);

        if (ctx.getVehicle().getStatus() != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Vehicle with id " + request.getVehicleId() + " is not available for reservation (status: " + ctx.getVehicle().getStatus() + ")"
            );
        }

        List<Reservation> overlapping = repository.findOverlapping(
                request.getVehicleId(),
                ReservationStatus.ACTIVE,
                request.getStartDate(),
                request.getEndDate()
        );
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException(
                    "Vehicle with id " + request.getVehicleId() + " already has an active reservation overlapping the requested period."
            );
        }

        Reservation reservation = mapper.toEntity(request, ctx);
        reservation.setCreatedBy(createdBy);

        ctx.getVehicle().setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(ctx.getVehicle());

        Reservation savedReservation = repository.save(reservation);
        paymentService.createForReservation(savedReservation, request.getPayment());
        PaymentResponse paymentResponse = paymentService.getForReservation(savedReservation.getId());

        String tenantSlug = TenantContext.getTenantId();
        contractPdfService.generateAsync(savedReservation.getId(), tenantSlug);

        return mapper.toResponse(savedReservation, paymentResponse);
    }

    @Override
    public List<ReservationResponse> getAll() {
        return repository.findAll().stream()
                .map(r -> mapper.toResponse(r, paymentService.findForReservation(r.getId()).orElse(null)))
                .toList();
    }

    @Override
    public PagedResponse<ReservationResponse> getAll(ReservationStatus status, ContractStatus contractStatus, String customerId, String vehicleId, LocalDateTime startDateFrom, LocalDateTime startDateTo, Pageable pageable) {
        Specification<Reservation> spec = ReservationSpecification.withFilters(status, contractStatus, customerId, vehicleId, startDateFrom, startDateTo);
        return PagedResponseMapper.toPagedResponse(
                repository.findAll(spec, pageable),
                r -> mapper.toResponse(r, paymentService.findForReservation(r.getId()).orElse(null))
        );
    }

    @Override
    public ReservationResponse get(String id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));
        PaymentResponse paymentResponse = paymentService.findForReservation(id).orElse(null);
        return mapper.toResponse(reservation, paymentResponse);
    }

    @Override
    @Transactional
    public void close(String id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));

        reservation.setStatus(ReservationStatus.CLOSED);

        boolean crossHub = !reservation.getPickupHub().getId().equals(reservation.getReturnHub().getId());
        VehicleStatus newVehicleStatus = crossHub ? VehicleStatus.PENDING_RELOCATION : VehicleStatus.AVAILABLE;

        reservation.getVehicle().setStatus(newVehicleStatus);
        vehicleRepository.save(reservation.getVehicle());

        repository.save(reservation);

        String tenantSlug = TenantContext.getTenantId();
        invoicePdfService.generateAsync(reservation.getId(), tenantSlug);
    }

    @Override
    @Transactional
    public void cancel(String id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservation.getVehicle().setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(reservation.getVehicle());

        repository.save(reservation);
    }

    @Override
    @Transactional
    public void updateContractStatus(String id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));
        reservation.setContractStatus(deriveContractStatus(reservation.isDigitallySigned(), reservation.isPhysicallyPrinted()));
        repository.save(reservation);
    }

    @Override
    @Transactional
    public void sign(String id, String signatureBase64) {
        if (signatureBase64.length() > 500 * 1024) {
            throw new IllegalArgumentException("Signature Base64 payload exceeds the 500KB limit");
        }
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));
        reservation.setDigitallySigned(true);
        reservation.setSignatureBase64(signatureBase64);
        reservation.setContractStatus(deriveContractStatus(true, reservation.isPhysicallyPrinted()));
        repository.save(reservation);
    }

    @Override
    @Transactional
    public void markPrinted(String id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));
        reservation.setPhysicallyPrinted(true);
        reservation.setContractStatus(deriveContractStatus(reservation.isDigitallySigned(), true));
        repository.save(reservation);
    }

    private ContractStatus deriveContractStatus(boolean digitallySigned, boolean physicallyPrinted) {
        if (digitallySigned && physicallyPrinted) return ContractStatus.FULLY_EXECUTED;
        if (digitallySigned || physicallyPrinted) return ContractStatus.PARTIAL_EXECUTION;
        return ContractStatus.PENDING;
    }
}
