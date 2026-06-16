package com.rently.rently.calendar;

import com.rently.rently.fleet.vehicles.Vehicle;
import com.rently.rently.fleet.vehicles.VehicleRepository;
import com.rently.rently.location.hub.HubRepository;
import com.rently.rently.reservation.booking.BookingRequest;
import com.rently.rently.reservation.booking.BookingRequestRepository;
import com.rently.rently.reservation.reservation.Reservation;
import com.rently.rently.reservation.reservation.ReservationRepository;
import com.rently.rently.reservation.reservation.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarServiceImplementation implements CalendarService {

    private final ReservationRepository reservationRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final VehicleRepository vehicleRepository;
    private final HubRepository hubRepository;

    @Override
    public List<CalendarEventResponse> getEvents(LocalDate from, LocalDate to, String branchId) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        if (ChronoUnit.DAYS.between(from, to) > 366) {
            throw new IllegalArgumentException("Date range must not exceed 366 days");
        }

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();

        List<CalendarEventResponse> events = new ArrayList<>();

        List<ReservationStatus> reservationStatuses = List.of(ReservationStatus.ACTIVE, ReservationStatus.CLOSED);
        List<Reservation> reservations = branchId != null
                ? reservationRepository.findForCalendarByBranch(reservationStatuses, fromDt, toDt, branchId)
                : reservationRepository.findForCalendar(reservationStatuses, fromDt, toDt);
        for (Reservation r : reservations) {
            events.add(mapReservation(r));
        }

        List<BookingRequest> bookingRequests = branchId != null
                ? bookingRequestRepository.findPendingForCalendarByBranch(fromDt, toDt, branchId)
                : bookingRequestRepository.findPendingForCalendar(fromDt, toDt);
        for (BookingRequest br : bookingRequests) {
            events.add(mapBookingRequest(br));
        }

        List<String> hubIds = branchId != null ? hubRepository.findIdsByBranchId(branchId) : null;
        List<Vehicle> vehiclesWithExpiry;
        if (hubIds != null && hubIds.isEmpty()) {
            vehiclesWithExpiry = List.of();
        } else if (hubIds != null) {
            vehiclesWithExpiry = vehicleRepository.findByInsuranceExpiresAtBetweenAndCurrentHubIdIn(from, to, hubIds);
        } else {
            vehiclesWithExpiry = vehicleRepository.findByInsuranceExpiresAtBetween(from, to);
        }
        for (Vehicle v : vehiclesWithExpiry) {
            events.add(mapInsuranceExpiry(v));
        }

        return events;
    }

    private CalendarEventResponse mapReservation(Reservation r) {
        String customerName = r.getCustomer().getFirstName() + " " + r.getCustomer().getLastName();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("customerName", customerName);
        metadata.put("status", r.getStatus().name());
        return new CalendarEventResponse(
                CalendarEventType.RESERVATION,
                r.getId(),
                r.getVehicle().getId(),
                r.getVehicle().getLicensePlate(),
                r.getStartDate(),
                r.getEndDate(),
                customerName,
                metadata
        );
    }

    private CalendarEventResponse mapBookingRequest(BookingRequest br) {
        String clientName = br.getClientAccount().getFirstName() + " " + br.getClientAccount().getLastName();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("clientName", clientName);
        return new CalendarEventResponse(
                CalendarEventType.BOOKING_REQUEST,
                br.getId(),
                br.getVehicle().getId(),
                br.getVehicle().getLicensePlate(),
                br.getStartDate(),
                br.getEndDate(),
                clientName,
                metadata
        );
    }

    private CalendarEventResponse mapInsuranceExpiry(Vehicle v) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), v.getInsuranceExpiresAt());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("expiresAt", v.getInsuranceExpiresAt().toString());
        metadata.put("daysRemaining", daysRemaining);
        return new CalendarEventResponse(
                CalendarEventType.INSURANCE_EXPIRY,
                null,
                v.getId(),
                v.getLicensePlate(),
                null,
                null,
                v.getLicensePlate() + " - Insurance Expiry",
                metadata
        );
    }
}
