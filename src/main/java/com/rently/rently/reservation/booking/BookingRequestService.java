package com.rently.rently.reservation.booking;

import com.rently.rently.reservation.booking.dto.BookingRequestResponse;
import com.rently.rently.reservation.booking.dto.SubmitBookingRequestRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookingRequestService {
    BookingRequestResponse submit(String clientId, SubmitBookingRequestRequest request,
                                  MultipartFile idDocumentFile, MultipartFile driverLicenseDocumentFile);
    BookingRequestResponse confirm(String requestId, String agentUserId);
    void reject(String requestId, String agentUserId, String rejectionReason);
    List<BookingRequestResponse> getForClient(String clientId);
    BookingRequestResponse getForClient(String clientId, String requestId);
    List<BookingRequestResponse> getAll(BookingRequestStatus status);
    BookingRequestResponse get(String requestId);
}
