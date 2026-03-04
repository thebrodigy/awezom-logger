package io.github.thebrodigy.awezomlogger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AwezomLog {
    String[] mask() default {};
    boolean logRequest() default false;
    boolean logResponse() default false;
    boolean includeArgs() default true;
    boolean includeResult() default true;
    boolean includeExecutionTime() default true;
    int slowCallThreshold() default 1000;
    boolean logHeaders() default false;
    String format() default "COMPACT";
    String condition() default "";
}