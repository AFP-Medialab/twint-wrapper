package com.afp.medialab.weverify.social.constrains;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StatusValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusConstrain {
    String message() default "Invalid status in JSON (\"Pending\", \"Running\", \"Error\", \"CountingWords\", \"Done\"";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
