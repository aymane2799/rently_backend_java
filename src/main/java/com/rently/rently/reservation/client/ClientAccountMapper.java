package com.rently.rently.reservation.client;

import com.rently.rently.reservation.client.dto.RegisterClientRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientAccountMapper {

    public ClientAccount toEntity(RegisterClientRequest request, String passwordHash) {
        return ClientAccount.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordHash)
                .build();
    }
}
