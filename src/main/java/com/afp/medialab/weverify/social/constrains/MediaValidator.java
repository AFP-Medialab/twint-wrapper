package com.afp.medialab.weverify.social.constrains;

import com.afp.medialab.weverify.social.controller.TwitterGatewayServiceController;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.Arrays;

public class MediaValidator implements ConstraintValidator<MediaConstrain, String> {

    private ArrayList<String> medias = new ArrayList<>(
            Arrays.asList(null, "video", "image", "both")
    );
    @Override
    public void initialize(MediaConstrain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return medias.contains(s);
    }
}
