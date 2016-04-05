package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.NoMatchingViewException;
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

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class EditFiltersTest {

    private static String FILTER_NAME = "dummy filter";
    private static String CAL_RULE = "cal rule";
    private static String PATTERNS_RULE = "patterns rule";
    private static String ACTION_NAME = "action name";
    private static String TEST_CAL_RULE = "My rule for testing";
    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @Rule
    public final ActivityTestRule<CallBlockerManager> myActivityRule = new ActivityTestRule<>(CallBlockerManager.class);

    @Before
    public void before() {
        FilterHandle filter = new FilterHandle(FILTER_NAME,CAL_RULE, PATTERNS_RULE, ACTION_NAME);
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterProvider.InsertRow(db, filter);
        CalendarRule crule = new CalendarRule(TEST_CAL_RULE,CalendarRule.makeMask(9), 1,2,23,22);
        CalendarRuleProvider.InsertRow(db,crule);
        db.close();
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterProvider.DeleteFilter(db, FILTER_NAME);
        CalendarRuleProvider.DeleteCalendarRule(db,TEST_CAL_RULE);
        db.close();
    }

    @Test
    public void SmokeTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilters.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the filter is displayed
        onView(ViewMatchers.withId(R.id.text_filter_name)).check(matches(withText(FILTER_NAME)));
        //test that the add action is present
        onView(ViewMatchers.withId(R.id.action_new_item)).check(matches(isDisplayed()));
    }

    @Test
    public void SelectFilterTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilters.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the filter is displayed
        onView(ViewMatchers.withId(R.id.text_filter_name)).check(matches(withText(FILTER_NAME)));
        //now select it
        onView(ViewMatchers.withId(R.id.text_filter_name)).perform(click());
        //check the current activity
        Activity activity = InstrumentTestHelper.getCurrentActivity();
        assertEquals("Check the current running activity",NewEditFilter.class.getCanonicalName(),activity.getClass().getCanonicalName());
        onView(ViewMatchers.withId(R.id.text_calendar_name)).check(matches(withText(containsString(CAL_RULE))));
        onView(ViewMatchers.withId(R.id.text_filter_rule_name)).check(matches(withText(containsString(PATTERNS_RULE))));
        onView(ViewMatchers.withId(R.id.text_action_name)).check(matches(withText(containsString(ACTION_NAME))));
    }

    @Test
    public void ChangeActionTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilters.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the filter is displayed
        onView(ViewMatchers.withId(R.id.text_filter_name)).check(matches(withText(FILTER_NAME)));
        //now select it
        onView(ViewMatchers.withId(R.id.text_filter_name)).perform(click());
        //check that the change button is active
        onView(ViewMatchers.withId(R.id.action_change)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.text_calendar_name)).check(matches(not(isEnabled())));
        //now click it
        onView(ViewMatchers.withId(R.id.action_change)).perform(click());
        onView(ViewMatchers.withId(R.id.text_calendar_name)).check(matches(isEnabled()));
    }

    @Test
    public void DeleteActionTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilters.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the filter is displayed
        onView(ViewMatchers.withId(R.id.text_filter_name)).check(matches(withText(FILTER_NAME)));
        //now select it
        onView(ViewMatchers.withId(R.id.text_filter_name)).perform(click());
        //check that the change button is active
        onView(ViewMatchers.withId(R.id.action_delete)).check(matches(isDisplayed()));
        //click the delete rule
        onView(ViewMatchers.withId(R.id.action_delete)).perform(click());
        //a dialog confirmation should appear
        onView(ViewMatchers.withText(myActivityRule.getActivity().getString(R.string.tx_filter_delete_confirm)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void UndoActionTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilters.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the filter is displayed
        onView(ViewMatchers.withId(R.id.text_filter_name)).check(matches(withText(FILTER_NAME)));
        //now select it
        onView(ViewMatchers.withId(R.id.text_filter_name)).perform(click());
        //check that the change button is active
        onView(ViewMatchers.withId(R.id.action_undo)).check(doesNotExist());
        onView(ViewMatchers.withId(R.id.action_change)).perform(click());
        //now pick a new calendar rule
        Activity activity = InstrumentTestHelper.getCurrentActivity();
        openActionBarOverflowOrOptionsMenu(activity);
        onView(withText("Pick a calendar rule")).perform(click());
        //now select a new calendar rule
        onView(withText(TEST_CAL_RULE)).perform(click());
        activity = InstrumentTestHelper.getCurrentActivity();
        assertEquals("Back in EditFilters activity", NewEditFilter.class.getCanonicalName(), activity.getClass().getCanonicalName());
        try {
            onView(ViewMatchers.withId(R.id.action_undo)).check(matches(isDisplayed()));
        }
        catch (NoMatchingViewException e) {
            openActionBarOverflowOrOptionsMenu(activity);
            onView(withText("Undo a change")).perform(click());
        }
        onView(ViewMatchers.withId(R.id.action_undo)).check(doesNotExist());
        onView(ViewMatchers.withId(R.id.action_change)).check(matches(isDisplayed()));
    }
}
