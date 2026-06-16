package com.rently.rently.reservation.booking;

import com.rently.rently.auth.jwt.ClientJwtTokenProvider;
import com.rently.rently.multitenancy.TenantContext;
import com.rently.rently.reservation.booking.dto.BookingRequestResponse;
import com.rently.rently.reservation.booking.dto.SubmitBookingRequestRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/{slug}/booking-requests")
@RequiredArgsConstructor
public class PublicBookingRequestController {

    private final BookingRequestService bookingRequestService;
    private final ClientJwtTokenProvider clientJwtTokenProvider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BookingRequestResponse submit(
            @PathVariable String slug,
            @ModelAttribute @Valid SubmitBookingRequestRequest request,
            @RequestPart("idDocumentFile") MultipartFile idDocumentFile,
            @RequestPart("driverLicenseDocumentFile") MultipartFile driverLicenseDocumentFile,
            @RequestHeader("Authorization") String authHeader) {
        String clientId = extractClientId(authHeader, slug);
        TenantContext.setTenantId(slug);
        return bookingRequestService.submit(clientId, request, idDocumentFile, driverLicenseDocumentFile);
    }

    @GetMapping
    public List<BookingRequestResponse> listOwn(
            @PathVariable String slug,
            @RequestHeader("Authorization") String authHeader) {
        String clientId = extractClientId(authHeader, slug);
        TenantContext.setTenantId(slug);
        return bookingRequestService.getForClient(clientId);
    }

    @GetMapping("/{id}")
    public BookingRequestResponse getOwn(
            @PathVariable String slug,
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        String clientId = extractClientId(authHeader, slug);
        TenantContext.setTenantId(slug);
        return bookingRequestService.getForClient(clientId, id);
    }

    private String extractClientId(String authHeader, String expectedSlug) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization header");
        }
        String token = authHeader.substring(7);
        if (!clientJwtTokenProvider.isTokenValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        String agencySlug = clientJwtTokenProvider.extractAgencySlug(token);
        if (!expectedSlug.equals(agencySlug)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token does not match the requested agency");
        }
        return clientJwtTokenProvider.extractClientId(token);
    }
}
