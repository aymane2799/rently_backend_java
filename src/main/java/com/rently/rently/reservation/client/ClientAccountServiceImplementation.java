package com.rently.rently.reservation.client;

import com.rently.rently.auth.jwt.ClientJwtTokenProvider;
import com.rently.rently.multitenancy.TenantContext;
import com.rently.rently.reservation.client.dto.ClientAuthResponse;
import com.rently.rently.reservation.client.dto.ClientLoginRequest;
import com.rently.rently.reservation.client.dto.RegisterClientRequest;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientAccountServiceImplementation implements ClientAccountService {

    private final ClientAccountRepository clientAccountRepository;
    private final ClientAccountMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final ClientJwtTokenProvider clientJwtTokenProvider;

    @Override
    @Transactional
    public ClientAuthResponse register(String agencySlug, RegisterClientRequest request) {
        TenantContext.setTenantId(agencySlug);
        try {
            if (clientAccountRepository.existsByEmail(request.getEmail())) {
                throw new EntityExistsException("Email already registered: " + request.getEmail());
            }
            String hash = passwordEncoder.encode(request.getPassword());
            ClientAccount client = clientAccountRepository.save(mapper.toEntity(request, hash));
            return buildAuthResponse(client, agencySlug);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public ClientAuthResponse login(String agencySlug, ClientLoginRequest request) {
        TenantContext.setTenantId(agencySlug);
        try {
            ClientAccount client = clientAccountRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("Invalid credentials"));
            if (!client.isActive()) {
                throw new IllegalArgumentException("Account is inactive");
            }
            if (!passwordEncoder.matches(request.getPassword(), client.getPasswordHash())) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            return buildAuthResponse(client, agencySlug);
        } finally {
            TenantContext.clear();
        }
    }

    private ClientAuthResponse buildAuthResponse(ClientAccount client, String agencySlug) {
        String token = clientJwtTokenProvider.generateToken(client, agencySlug);
        return ClientAuthResponse.builder()
                .token(token)
                .clientId(client.getId())
                .email(client.getEmail())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .agencySlug(agencySlug)
                .build();
    }
}
