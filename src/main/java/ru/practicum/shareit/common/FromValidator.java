package ru.practicum.shareit.common;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FromValidator implements ConstraintValidator<ValidateFromIfPresent, Integer> {

    @Override
    public void initialize(ValidateFromIfPresent constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        if (integer == null) {
            return true;
        }
        return (integer >= 0);
    }
}
