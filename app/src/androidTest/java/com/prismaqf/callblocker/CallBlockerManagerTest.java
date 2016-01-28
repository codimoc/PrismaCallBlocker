package com.prismaqf.callblocker;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
public class CallBlockerManagerTest
{
    @Rule
    private ActivityTestRule<CallBlockerManager> mActivityRule = new ActivityTestRule(CallBlockerManager.class);

    @Test
    public void checkDisplay() {
        //simple Espresso test just to check that it all works.
        //This just test that the button appears on the screen
        onView(withId(R.id.buttonDetectToggle)).check(matches(isDisplayed()));
    }

}
