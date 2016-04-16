package com.prismaqf.callblocker;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;
import com.prismaqf.callblocker.utils.DebugDBFileName;
import com.prismaqf.callblocker.utils.InstrumentTestHelper;
import com.prismaqf.callblocker.utils.ToastMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UpdateCalendarRuleTest {

    private long myRuleId;
    private static final String TEST_RULE = "My rule for testing";
    private static final String TEST_RULE_IN_FILTER = "My rule belonging to a filter";
    private static final String TEST_FILTER = "My filter for testing";

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    //Make the rule but don't start the activity
    @Rule
    public final ActivityTestRule<CallBlockerManager> myActivityRule = new ActivityTestRule<>(CallBlockerManager.class);

    @Before
    public void before() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        CalendarRuleProvider.DeleteCalendarRule(db, TEST_RULE);
        myRuleId = CalendarRuleProvider.InsertRow(db, new CalendarRule(TEST_RULE,CalendarRule.makeMask(9), 1,2,23,22));
        CalendarRuleProvider.InsertRow(db, new CalendarRule(TEST_RULE_IN_FILTER,CalendarRule.makeMask(0)));
        FilterProvider.InsertRow(db, new FilterHandle(TEST_FILTER,TEST_RULE_IN_FILTER,"dummy1","dummy2"));
        db.close();
        Intent intent = new Intent(myActivityRule.getActivity(),EditCalendarRules.class);
        myActivityRule.getActivity().startActivity(intent); //relaunch
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        CalendarRuleProvider.DeleteCalendarRule(db, myRuleId);
        CalendarRuleProvider.DeleteCalendarRule(db, TEST_RULE_IN_FILTER);
        FilterProvider.DeleteFilter(db,TEST_FILTER);
        db.close();
    }


    @Test
    public void TestSelectFromListOfExisting() throws Throwable {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(not(isEnabled())));
        Activity currentActivity = InstrumentTestHelper.getCurrentActivity();
        assertEquals("Enetered the NewEditCalendarRule activity", NewEditCalendarRule.class, currentActivity.getClass());
    }

    @Test
    public void TestActions() {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.action_save)).check(doesNotExist());
        onView(ViewMatchers.withId(R.id.action_delete)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.action_change)).check(matches(isDisplayed()));
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
        onView(ViewMatchers.withId(R.id.action_change)).perform(click());
        //now wait until the activity is ready. This is done with a IdleResource pattern
        //this should not really needed in Espresso, but it is
        //now the view is idle, continue with the tests. Check that the button below is enables
        onView(ViewMatchers.withId(R.id.bt_no_days)).check(matches(isEnabled()));
        //and that the delete button has disappeared
        onView(ViewMatchers.withId(R.id.action_delete)).check(doesNotExist());
        //and that there is no save action  (no changes yet)
        onView(ViewMatchers.withId(R.id.action_save)).check(doesNotExist());
        //now clivk on the "no days" button to perform changes
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        //the save action should appear
        onView(ViewMatchers.withId(R.id.action_save)).check(matches(isDisplayed()));
    }

    @Test
    public void TestDeleteAction() throws Throwable {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.action_delete)).check(matches(isEnabled()));
        //click the delete rule
        onView(ViewMatchers.withId(R.id.action_delete)).perform(click());
        //a dialog confirmation should appear
        onView(ViewMatchers.withText(myActivityRule.getActivity().getString(R.string.tx_rule_delete_confirm)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void TestDeleteRuleWhenUsedShouldFlag() {
        onView(ViewMatchers.withText(TEST_RULE_IN_FILTER)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE_IN_FILTER)).perform(click());
        onView(ViewMatchers.withId(R.id.action_delete)).perform(click());
        onView(ViewMatchers.withText(myActivityRule.getActivity().getString(R.string.tx_rule_delete_confirm)))
                .check(matches(isDisplayed()));
        onView(ViewMatchers.withText(containsString("Yes"))).perform(click());
        ToastMatcher matcher = new ToastMatcher("toast!");
        onView(ViewMatchers.withText(R.string.msg_can_not_delete_rule)).inRoot(matcher).check(matches(isDisplayed()));
    }

    @Test
    public void TestUndoAction() throws Throwable {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        //go in edit mode
        onView(ViewMatchers.withId(R.id.action_change)).perform(click());
        //now make a change
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        //the change action is not displayed
        onView(ViewMatchers.withId(R.id.action_change)).check(doesNotExist());
        //but save and undo should appear after the change
        onView(ViewMatchers.withId(R.id.action_save)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.action_undo)).check(matches(isDisplayed()));
        //now undo
        onView(ViewMatchers.withId(R.id.action_undo)).perform(click());
        //change button should re-appear
        onView(ViewMatchers.withId(R.id.action_change)).check(matches(isDisplayed()));
        //and undo disappear
        onView(ViewMatchers.withId(R.id.action_undo)).check(doesNotExist());
    }
}
