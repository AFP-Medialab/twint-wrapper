package com.afp.medialab.weverify.social.constrains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;

public class StatusValidator implements ConstraintValidator<StatusConstrain, String> {

    private ArrayList<String> status = new ArrayList<>(
            Arrays.asList(null, "Pending", "Running", "Error", "CountingWords", "Done")
    );

    @Override
    public void initialize(StatusConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return status.contains(s);
    }
}
