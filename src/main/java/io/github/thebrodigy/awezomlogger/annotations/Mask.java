package io.github.thebrodigy.awezomlogger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mask {
    String pattern() default "DEFAULT";
    int prefix() default 0;
    int suffix() default 0;
}