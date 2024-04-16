package net.moewes.cloudui.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
@Repeatable(JavaScripts.class)
public @interface JavaScript {
    String value();

    String id() default "";
}
