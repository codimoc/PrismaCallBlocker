package com.prismaqf.callblocker;


import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CallBlockerManagerTest
{
    @Rule
    public ActivityTestRule<CallBlockerManager> mActivityRule = new ActivityTestRule(CallBlockerManager.class);

    @Test
    public void checkDisplay() {
        //simple Espresso test just to check that it all works.
        //This just test that the button appears on the screen
        onView(withId(R.id.buttonDetectToggle)).check(matches(isDisplayed()));
    }

    @Test
    public void checkThatTurningOnServiceChangesText() {
        //first is off, check not detecting lables
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.no_detect)));
        onView(withId(R.id.buttonDetectToggle)).check(matches(withText(R.string.turn_on)));
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        //now check that the text has changed
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.detect)));
        onView(withId(R.id.buttonDetectToggle)).check(matches(withText(R.string.turn_off)));
        onView(withId(R.id.buttonDetectToggle)).perform(click()); //turn off
    }

    @Test
    public void checkStatusOfService() {
        Activity myActivity = mActivityRule.getActivity();
        assertFalse("service off", CallBlockerManager.isServiceRunning(myActivity));
        //turn it on
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        assertTrue("service on", CallBlockerManager.isServiceRunning(myActivity));
        //turn it off
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        assertFalse("service off",CallBlockerManager.isServiceRunning(myActivity));
    }



}
