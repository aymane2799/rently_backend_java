package com.rently.rently.reservation.client;

import com.rently.rently.reservation.client.dto.ClientAuthResponse;
import com.rently.rently.reservation.client.dto.ClientLoginRequest;
import com.rently.rently.reservation.client.dto.RegisterClientRequest;

public interface ClientAccountService {
    ClientAuthResponse register(String agencySlug, RegisterClientRequest request);
    ClientAuthResponse login(String agencySlug, ClientLoginRequest request);
}
