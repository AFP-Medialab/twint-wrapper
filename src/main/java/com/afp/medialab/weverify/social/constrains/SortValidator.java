package com.afp.medialab.weverify.social.constrains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SortValidator implements ConstraintValidator<SortConstrain, String> {
    @Override
    public void initialize(SortConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return (s.equals("desc") || s.equals("asc"));
    }
}
