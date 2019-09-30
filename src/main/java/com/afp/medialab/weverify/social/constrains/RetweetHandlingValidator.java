package com.afp.medialab.weverify.social.constrains;

import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class RetweetHandlingValidator implements ConstraintValidator<RetweetHandlingConstrain, String> {

    //private static org.slf4j.Logger Logger = LoggerFactory.getLogger(RetweetHandlingValidator.class);
    
    private ArrayList<String> retweetModes = new ArrayList<>(
            Arrays.asList(null, "allowed", "only", "excluded")
    );



    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return retweetModes.contains(s);

    }
}
