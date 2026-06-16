package com.rently.rently.agency;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agencies")
@RequiredArgsConstructor
public class PublicQrCodeController {

    private final AgencyRepository agencyRepository;
    private final QrCodeService qrCodeService;

    @Value("${app.public-base-url:http://localhost:3000}")
    private String publicBaseUrl;

    @GetMapping(value = "/{slug}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getQrCode(@PathVariable String slug) {
        agencyRepository.findBySlug(slug)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Agency not found: " + slug));
        String landingPageUrl = publicBaseUrl + "/" + slug;
        return qrCodeService.generatePng(landingPageUrl, 300);
    }
}
