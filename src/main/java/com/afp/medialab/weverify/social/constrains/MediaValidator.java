package com.afp.medialab.weverify.social.constrains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MediaValidator implements ConstraintValidator<MediaConstrain, String> {

    @Override
    public void initialize(MediaConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return (s == null || s.equals("video") || s.equals("image") || s.equals("both"));
    }
}
