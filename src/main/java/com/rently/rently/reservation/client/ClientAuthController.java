package com.rently.rently.reservation.client;

import com.rently.rently.reservation.client.dto.ClientAuthResponse;
import com.rently.rently.reservation.client.dto.ClientLoginRequest;
import com.rently.rently.reservation.client.dto.RegisterClientRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/{slug}/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAccountService clientAccountService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientAuthResponse register(
            @PathVariable String slug,
            @Valid @RequestBody RegisterClientRequest request) {
        return clientAccountService.register(slug, request);
    }

    @PostMapping("/login")
    public ClientAuthResponse login(
            @PathVariable String slug,
            @Valid @RequestBody ClientLoginRequest request) {
        return clientAccountService.login(slug, request);
    }
}
