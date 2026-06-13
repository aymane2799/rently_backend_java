package com.rently.rently.reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignatureRequest {

    @NotBlank
    private String signatureBase64;
}
