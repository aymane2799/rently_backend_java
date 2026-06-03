package com.rently.rently.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<ValidEnum, CharSequence> {

    private List<String> acceptedValues;
    private boolean ignoreCase;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        ignoreCase = constraintAnnotation.ignoreCase();
        Enum<?>[] enumConstants = constraintAnnotation.enumClass().getEnumConstants();
        initializeAcceptedValues(enumConstants);
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value != null) {
            return checkIfValueTheSame(acceptedValues, value.toString());
        }
        return true;
    }

    protected boolean checkIfValueTheSame(List<String> acceptedValues, String value) {
        for (String acceptedValue : acceptedValues) {
            if (ignoreCase && acceptedValue.equalsIgnoreCase(value)) {
                return true;
            } else if (acceptedValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    protected void initializeAcceptedValues(Enum<?>... enumConstants) {
        if (enumConstants == null || enumConstants.length == 0) {
            acceptedValues = Collections.emptyList();
        } else {
            acceptedValues = Stream.of(enumConstants)
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
    }
}