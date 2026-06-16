package com.rently.rently.reservation.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientLoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
