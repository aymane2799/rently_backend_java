package com.rently.rently.validation;

import com.rently.rently.reservation.payment.CreatePaymentRequest;
import com.rently.rently.reservation.payment.DepositType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DepositFieldsValidator implements ConstraintValidator<ValidDepositFields, CreatePaymentRequest> {

    @Override
    public boolean isValid(CreatePaymentRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getDepositType() == null) {
            return true;
        }
        if (request.getDepositType() == DepositType.CHEQUE) {
            return request.getChequeNumber() != null && !request.getChequeNumber().isBlank();
        }
        if (request.getDepositType() == DepositType.CREDIT_CARD_PREAUTH) {
            return request.getCreditCardAuthReference() != null && !request.getCreditCardAuthReference().isBlank();
        }
        return true;
    }
}
