package com.rently.rently.calendar;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<CalendarEventResponse> getEvents(LocalDate from, LocalDate to, String branchId);
}
