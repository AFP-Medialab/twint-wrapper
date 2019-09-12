package com.afp.medialab.weverify.social.constrains;

import com.afp.medialab.weverify.social.controller.TwitterGatewayServiceController;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MediaValidator implements ConstraintValidator<MediaConstrain, String> {
    private static org.slf4j.Logger Logger = LoggerFactory.getLogger(TwitterGatewayServiceController.class);

    @Override
    public void initialize(MediaConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return (s == null || s == "video" || s == "image" || s == "both");

    }
}
