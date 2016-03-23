package com.prismaqf.callblocker.actions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to make actions available at run time
 * @author ConteDiMonteCristo
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AvailableAction {
    String description() default "undefined";
}
