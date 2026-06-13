package com.rently.rently.reservation.reservation;

import com.rently.rently.fleet.vehicles.VehicleMapper;
import com.rently.rently.location.hub.HubMapper;
import com.rently.rently.reservation.customer.CustomerMapper;
import com.rently.rently.reservation.payment.PaymentMapper;
import com.rently.rently.reservation.payment.PaymentResponse;
import com.rently.rently.reservation.reservation.hydration.ReservationHydrationContext;
import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationMapper implements
        ResponseMapper<Reservation, ReservationResponse>,
        CreateMapper<Reservation, CreateReservationRequest, ReservationHydrationContext> {

    private final CustomerMapper customerMapper;
    private final VehicleMapper vehicleMapper;
    private final HubMapper hubMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public Reservation toEntity(CreateReservationRequest request, ReservationHydrationContext ctx) {
        return Reservation.builder()
                .customer(ctx.getCustomer())
                .vehicle(ctx.getVehicle())
                .pickupHub(ctx.getPickupHub())
                .returnHub(ctx.getReturnHub())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(request.getTotalAmount())
                .build();
    }

    @Override
    public ReservationResponse toResponse(Reservation entity) {
        return toResponse(entity, null);
    }

    public ReservationResponse toResponse(Reservation entity, PaymentResponse paymentResponse) {
        return ReservationResponse.builder()
                .id(entity.getId())
                .customer(customerMapper.toResponse(entity.getCustomer()))
                .vehicle(vehicleMapper.toResponse(entity.getVehicle()))
                .pickupHub(hubMapper.toResponse(entity.getPickupHub()))
                .returnHub(hubMapper.toResponse(entity.getReturnHub()))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .isDigitallySigned(entity.isDigitallySigned())
                .isPhysicallyPrinted(entity.isPhysicallyPrinted())
                .signatureBase64(entity.getSignatureBase64())
                .contractStatus(entity.getContractStatus())
                .createdBy(entity.getCreatedBy())
                .payment(paymentResponse)
                .contractUrl(entity.getContractUrl())
                .invoiceUrl(entity.getInvoiceUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
