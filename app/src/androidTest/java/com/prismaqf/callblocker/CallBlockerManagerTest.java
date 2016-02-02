package com.prismaqf.callblocker;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
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
    public final ActivityTestRule<CallBlockerManager> mActivityRule = new ActivityTestRule(CallBlockerManager.class);

    @Before
    public void before() {
        stopRunningService();
    }


    @Test
    public void checkDisplay() {
        //simple Espresso test just to check that it all works.
        //This just test that the button appears on the screen
        onView(withId(R.id.buttonDetectToggle)).check(matches(isDisplayed()));
    }

    @Test
    public void checkThatTurningOnServiceChangesText() {
        //first is off, check not detecting labels
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
        assertFalse("service off", CallBlockerManager.isServiceRunning(myActivity));
    }

    @Test
    public void checkServiceStateOnSharedPreferences() {
        Activity myActivity = mActivityRule.getActivity();
        Context myCtx = myActivity.getApplicationContext();
        SharedPreferences prefs = myCtx.getSharedPreferences(
                myActivity.getString(R.string.file_shared_prefs_name),
                Context.MODE_PRIVATE);
        //before running the service
        String state = prefs.getString(myActivity.getString(R.string.shared_prefs_key_state), "not found");
        assertEquals("idle state", "idle", state);
        //after starting the service
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        state = prefs.getString(myActivity.getString(R.string.shared_prefs_key_state),"not found");
        assertEquals("running state", "running", state);
        //after stopping the service
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        state = prefs.getString(myActivity.getString(R.string.shared_prefs_key_state),"not found");
        assertEquals("idle state", "idle", state);
    }

    @Test
    public void checkServiceIsBound() {
        CallBlockerManager myActivity = (CallBlockerManager)mActivityRule.getActivity();
        assertNull("The service not yet bound", myActivity.getService());
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        //now check that the text has changed
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.detect)));
        CallDetectService myService = myActivity.getService();
        assertNotNull("I can bind to the service",myService!=null);
        assertTrue("The service can be interrogated", myService.NumReceived() >= 0);
    }

    private void stopRunningService() {
        Activity myActivity = mActivityRule.getActivity();
        if (CallBlockerManager.isServiceRunning(myActivity)) {
            onView(withId(R.id.buttonDetectToggle)).perform(click());
        }
    }


}
