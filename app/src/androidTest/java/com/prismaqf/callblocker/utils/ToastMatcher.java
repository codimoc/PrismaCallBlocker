package com.prismaqf.callblocker.utils;

import android.os.IBinder;
import android.support.test.espresso.Root;
import android.view.WindowManager;

import org.hamcrest.CustomTypeSafeMatcher;


/**
 * onView(withText(R.string.mssage)).inRoot(new ToastMatcher()).check(matches(isDisplayed()));
 */
public class ToastMatcher extends CustomTypeSafeMatcher<Root> {
    public ToastMatcher(String description) {
        super(description);
    }

    @Override
    protected boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            if (windowToken == appToken) {
                // windowToken == appToken means this window isn't contained by any other windows.
                // if it was a window for an activity, it would have TYPE_BASE_APPLICATION.
                return true;
            }
        }
        return false;
    }
}
