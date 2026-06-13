package com.rently.rently.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DepositFieldsValidator.class)
public @interface ValidDepositFields {

    String message() default "chequeNumber is required for CHEQUE deposits; creditCardAuthReference is required for CREDIT_CARD_PREAUTH deposits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
