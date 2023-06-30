package ru.practicum.shareit.common;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FromValidator.class)
public @interface ValidateFromIfPresent {
    String message() default "From must be more or equal 0 if present";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
