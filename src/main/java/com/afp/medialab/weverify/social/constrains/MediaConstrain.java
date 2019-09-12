package com.afp.medialab.weverify.social.constrains;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MediaValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MediaConstrain {
    String message() default "Invalid media type in JSON";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
