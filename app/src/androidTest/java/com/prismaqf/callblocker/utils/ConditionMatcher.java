package com.prismaqf.callblocker.utils;

import android.support.test.espresso.core.deps.guava.base.Predicate;
import android.view.View;

import org.hamcrest.CustomMatcher;

/**
 * Generic boolean condition matcher based on Espresso Matcher
 * @author ConteDiMonteCristo
 */
class ConditionMatcher extends CustomMatcher<View> {

    private final Predicate condition;

    public ConditionMatcher(Predicate condition, String description) {
        super(description);
        this.condition = condition;
    }

    @Override
    public boolean matches(Object item) {
        return condition!=null && condition.apply(null);
    }
}
