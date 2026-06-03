package com.rently.rently.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {

    Class<? extends Enum<?>> enumClass();

    boolean ignoreCase() default false;

    String message() default "must be any of enum {enumClass}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}