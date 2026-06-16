package com.rently.rently.reservation.client.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientAuthResponse {
    private String token;
    private String clientId;
    private String email;
    private String firstName;
    private String lastName;
    private String agencySlug;
}
