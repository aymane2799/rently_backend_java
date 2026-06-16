package com.rently.rently.calendar;

import java.time.LocalDateTime;
import java.util.Map;

public record CalendarEventResponse(
        CalendarEventType type,
        String id,
        String vehicleId,
        String vehiclePlate,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String title,
        Map<String, Object> metadata
) {}
