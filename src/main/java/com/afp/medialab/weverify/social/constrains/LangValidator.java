package com.afp.medialab.weverify.social.constrains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LangValidator implements ConstraintValidator<LangConstrain, String> {
    @Override
    public void initialize(LangConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return s.matches("..");
    }
}
