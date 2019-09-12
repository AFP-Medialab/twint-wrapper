package com.afp.medialab.weverify.social.constrains;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RetweetHandlingValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RetweetHandlingConstrain {
    String message() default "Invalid retweet handling mode in JSON";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
