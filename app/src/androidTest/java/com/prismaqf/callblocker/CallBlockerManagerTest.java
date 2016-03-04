package com.prismaqf.callblocker;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.DbHelperTest;
import com.prismaqf.callblocker.utils.DebugHelper;
import com.prismaqf.callblocker.utils.ViewIdlingResource;

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
public class CallBlockerManagerTest extends DebugHelper
{
    static {
        DbHelper.SetDebugDb(myKey, DbHelperTest.DB_NAME);
    }

    @Rule
    public final ActivityTestRule<CallBlockerManager> mActivityRule = new ActivityTestRule<>(CallBlockerManager.class);

    @Before
    public void before() {
        View v = mActivityRule.getActivity().findViewById(R.id.textDetectState);
        IdlingResource idlingResource = new ViewIdlingResource(v);
        Espresso.registerIdlingResources(idlingResource);
        stopRunningService();
        Espresso.unregisterIdlingResources(idlingResource);
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
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.tx_no_detect)));
        onView(withId(R.id.buttonDetectToggle)).check(matches(withText(R.string.tx_turn_on)));
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        View v = mActivityRule.getActivity().findViewById(R.id.textDetectState);
        //wait using an IdlingResourse
        IdlingResource idlingResource = new ViewIdlingResource(v);
        Espresso.registerIdlingResources(idlingResource);
        //now check that the text has changed
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.tx_detect)));
        onView(withId(R.id.buttonDetectToggle)).check(matches(withText(R.string.tx_turn_off)));
        onView(withId(R.id.buttonDetectToggle)).perform(click()); //turn off
        Espresso.unregisterIdlingResources(idlingResource);
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
        String state = prefs.getString(myActivity.getString(R.string.pk_state), "not found");
        assertEquals("idle state", "idle", state);
        //after starting the service
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        state = prefs.getString(myActivity.getString(R.string.pk_state),"not found");
        assertEquals("running state", "running", state);
        //after stopping the service
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        state = prefs.getString(myActivity.getString(R.string.pk_state),"not found");
        assertEquals("idle state", "idle", state);
    }

    @Test
    public void testCallsBroadcastReceiver() {
        Context ctx = mActivityRule.getActivity().getApplicationContext();
        Intent intent = new Intent();
        intent.setAction(ctx.getString(R.string.ac_call));
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra(ctx.getString(R.string.ky_number_called), "123");
        intent.putExtra(ctx.getString(R.string.ky_received),10);
        intent.putExtra(ctx.getString(R.string.ky_triggered),5);
        ctx.sendBroadcast(intent);
        SystemClock.sleep(500);
        onView(withId(R.id.button_received)).check(matches(withText("10")));
        onView(withId(R.id.button_triggered)).check(matches(withText("5")));
    }

    private void stopRunningService() {
        Activity myActivity = mActivityRule.getActivity();
        /*       Intent intent = new Intent(myActivity, CallDetectService.class);
        myActivity.stopService(intent);*/
        if (CallBlockerManager.isServiceRunning(myActivity)) {
            onView(withId(R.id.buttonDetectToggle)).perform(click());
        }
    }


}
