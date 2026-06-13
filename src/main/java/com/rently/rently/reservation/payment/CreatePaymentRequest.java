package com.rently.rently.reservation.payment;

import com.rently.rently.validation.ValidDepositFields;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@ValidDepositFields
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePaymentRequest {

    @NotNull
    @PositiveOrZero
    private BigDecimal cashAdvanced;

    private String bankTransferReference;

    private String bankTransferImageUrl;

    @NotNull
    private DepositType depositType;

    @NotNull
    @PositiveOrZero
    private BigDecimal depositAmount;

    private String chequeNumber;

    private String creditCardAuthReference;
}
