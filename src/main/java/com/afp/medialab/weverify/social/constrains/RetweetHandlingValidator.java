package com.afp.medialab.weverify.social.constrains;

import com.afp.medialab.weverify.social.controller.TwitterGatewayServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class RetweetHandlingValidator implements ConstraintValidator<RetweetHandlingConstrain, String> {

    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);

    @Override
    public void initialize(RetweetHandlingConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return (s == null || s.equals("allowed") || s.equals("only") || s.equals("excluded"));

    }
}
