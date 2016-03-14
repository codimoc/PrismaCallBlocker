package com.prismaqf.callblocker.utils;

import android.support.test.espresso.IdlingResource;
import android.view.View;

/**
 * This utility class waits until a a view in the
 * target activity becomes idle. This is a clever
 * way for synchronizing Espresso when the normal
 * waiting (inside Espresso) does not work
 */
class ViewIdlingResource implements IdlingResource {
    private final View view;
    private ResourceCallback callback;

    /**
     * Constructor
     * @param view the view to synch against, waiting to become idle
     */
    private ViewIdlingResource(View view) {
        this.view = view;
    }

    @Override
    public String getName() {
        return String.format("%s:%s", ViewIdlingResource.class.getName(), view.toString());
    }

    @Override
    public boolean isIdleNow() {
        if(view.getVisibility() == View.VISIBLE && callback != null) {
            callback.onTransitionToIdle();
            return true;
        }
        return false;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        callback = resourceCallback;
    }
}