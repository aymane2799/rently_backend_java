package com.rently.rently.reservation.reservation;

import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.fleet.vehicles.VehicleStatus;
import com.rently.rently.reservation.reservation.hydration.ReservationHydrationContext;
import com.rently.rently.reservation.reservation.hydration.ReservationHydrator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImplementation implements ReservationService {

    private final ReservationRepository repository;
    private final VehicleRepository vehicleRepository;
    private final ReservationMapper mapper;
    private final ReservationHydrator hydrator;

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

        // TODO: Phase 9 — PaymentService.createForReservation(savedReservation, paymentRequest)

        return mapper.toResponse(repository.save(reservation));
    }

    @Override
    public List<ReservationResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ReservationResponse get(String id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation with id " + id + " not found!"));
        return mapper.toResponse(reservation);
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

        // TODO: Phase 11 — InvoicePdfService.generateAsync(reservation.getId())
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

    private ContractStatus deriveContractStatus(boolean digitallySigned, boolean physicallyPrinted) {
        if (digitallySigned && physicallyPrinted) return ContractStatus.FULLY_EXECUTED;
        if (digitallySigned || physicallyPrinted) return ContractStatus.PARTIAL_EXECUTION;
        return ContractStatus.PENDING;
    }
}
