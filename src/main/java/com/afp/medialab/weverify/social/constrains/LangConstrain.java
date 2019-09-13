package com.afp.medialab.weverify.social.constrains;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LangValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LangConstrain {

    String message() default "Invalid Lang type in JSON";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
