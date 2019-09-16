package com.afp.medialab.weverify.social.constrains;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;

public class StatusValidator implements ConstraintValidator<SortConstrain, String> {

    private ArrayList<String> status = new ArrayList<>(
            Arrays.asList("Pending", "Running", "Error", "Done")
    );
    @Override
    public void initialize(SortConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return status.contains(s);
    }
}
