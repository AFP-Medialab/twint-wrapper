package com.afp.medialab.weverify.social.constrains;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SortValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SortConstrain {
    String message() default "Invalid sort order in JSON (\"desc\", \"asc\"";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
