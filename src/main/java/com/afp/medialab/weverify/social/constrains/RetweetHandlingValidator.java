package com.afp.medialab.weverify.social.constrains;

import com.afp.medialab.weverify.social.controller.TwitterGatewayServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;


public class RetweetHandlingValidator implements ConstraintValidator<RetweetHandlingConstrain, String> {

    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);
    private ArrayList<String> retweetModes = new ArrayList<>(
            Arrays.asList(null, "allowed", "only", "excluded")
    );
    @Override
    public void initialize(RetweetHandlingConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return retweetModes.contains(s);

    }
}
