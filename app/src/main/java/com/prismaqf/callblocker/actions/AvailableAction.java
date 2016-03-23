package com.prismaqf.callblocker.actions;

/**
 * Annotation to make actions available at run time
 * @author ConteDiMonteCristo
 */
public @interface AvailableAction {
    String description() default "undefined";
}
