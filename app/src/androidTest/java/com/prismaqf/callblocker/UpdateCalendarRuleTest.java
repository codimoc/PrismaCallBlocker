package com.prismaqf.callblocker;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;

import com.prismaqf.callblocker.sql.CalendarRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.utils.ViewIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UpdateCalendarRuleTest {

    private long myRuleId;
    private static final String TEST_RULE = "My rule for testing";

    //Make the rule but don't start the activity
    @Rule
    public final ActivityTestRule<CallBlockerManager> myActivityRule = new ActivityTestRule(CallBlockerManager.class);

    @Before
    public void before() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        CalendarRule.DeleteCalendarRule(db,TEST_RULE);
        myRuleId = CalendarRule.InsertRow(db, TEST_RULE, 9, "01:02", "23:22");
        db.close();
        Intent intent = new Intent(myActivityRule.getActivity(),EditCalendarRules.class);
        myActivityRule.getActivity().startActivity(intent); //relaunch
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        CalendarRule.DeleteCalendarRule(db,myRuleId);
        db.close();
    }


    @Test
    public void TestSelectFromListOfExisting() throws Throwable {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(not(isEnabled())));
        Activity currentActivity = getCurrentActivity();
        assertEquals("Enetered the NewEditCalendarRule activity", NewEditCalendarRule.class, currentActivity.getClass());
    }

    @Test
    public void TestActions() {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(doesNotExist());
        onView(ViewMatchers.withId(R.id.action_delete_rule)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.action_change_rule)).check(matches(isDisplayed()));
    }

    @Test
    public void TestChangeAction() throws Throwable {
        //find the rule item in the list with the correct name
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        //click on the item to access the update activity
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        //in update mode all widgets should be disabled to start
        onView(ViewMatchers.withId(R.id.bt_no_days)).check(matches(not((isEnabled()))));
        //click on the change action to edit the rule
        onView(ViewMatchers.withId(R.id.action_change_rule)).perform(click());
        //now wait until the activity is ready. This is done with a IdleResource pattern
        //this should not really needed in Espresso, but it is
        Activity ca = getCurrentActivity();
        //need to attach to any view in the activity
        View v = ca.findViewById(R.id.edit_calendar_rule_name);
        //wait using an IdlingResourse
        IdlingResource idlingResource = new ViewIdlingResource(v);
        //now the view is idle, continue with the tests. Check that the button below is enables
        onView(ViewMatchers.withId(R.id.bt_no_days)).check(matches(isEnabled()));
        //and that the delete button has disappeared
        onView(ViewMatchers.withId(R.id.action_delete_rule)).check(doesNotExist());
        //and that there is no save action  (no changes yet)
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(doesNotExist());
        //now clivk on the "no days" button to perform changes
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        //the save action should appear
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(matches(isDisplayed()));
        //finally deregister
        Espresso.unregisterIdlingResources(idlingResource);
    }

    @Test
    public void TestDeleteAction() throws Throwable {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        View v = getCurrentActivity().findViewById(R.id.edit_calendar_rule_name);
        //wait using an IdlingResourse
        IdlingResource idlingResource = new ViewIdlingResource(v);
        onView(ViewMatchers.withId(R.id.action_delete_rule)).check(matches(isEnabled()));
        //click the delete rule
        onView(ViewMatchers.withId(R.id.action_delete_rule)).perform(click());
        //a dialog confirmation should appear
        onView(ViewMatchers.withText(myActivityRule.getActivity().getString(R.string.tx_calendar_rule_delete_confirm)))
                .check(matches(isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }

    @Test
    public void TestUndoAction() throws Throwable {
        Activity ca = getCurrentActivity();
        View v1 = ca.findViewById(R.id.list_fragment_holder);
        IdlingResource r1 = new ViewIdlingResource(v1);

        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        View v2 = getCurrentActivity().findViewById(R.id.edit_calendar_rule_name);
        //wait using an IdlingResourse
        IdlingResource r2 = new ViewIdlingResource(v2);
        //go in edit mode
        onView(ViewMatchers.withId(R.id.action_change_rule)).perform(click());
        //now make a change
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        //the change action is not displayed
        onView(ViewMatchers.withId(R.id.action_change_rule)).check(doesNotExist());
        //but save and undo should appear after the change
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.action_undo_rule)).check(matches(isDisplayed()));
        //now undo
        onView(ViewMatchers.withId(R.id.action_undo_rule)).perform(click());
        //change button should re-appear
        onView(ViewMatchers.withId(R.id.action_change_rule)).check(matches(isDisplayed()));
        //and undo disappear
        onView(ViewMatchers.withId(R.id.action_undo_rule)).check(doesNotExist());

        Espresso.unregisterIdlingResources(r1);
        Espresso.unregisterIdlingResources(r2);
    }


    private Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                java.util.Collection<Activity> activites = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                activity[0] = Iterables.getOnlyElement(activites);
            }
        });
        return activity[0];
    }


}
