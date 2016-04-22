package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;
import com.prismaqf.callblocker.sql.FilterRuleProvider;
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
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class UpdateFilterRuleTest {

    private long myRuleId;
    private static final String TEST_RULE = "My rule for testing";
    private static final String TEST_DESCRIPTION = "My rule description";
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
        FilterRuleProvider.DeleteFilterRule(db, TEST_RULE);
        myRuleId = FilterRuleProvider.InsertRow(db, new FilterRule(TEST_RULE,TEST_DESCRIPTION));
        FilterRuleProvider.InsertRow(db, new FilterRule(TEST_RULE_IN_FILTER, "dummy"));
        FilterProvider.InsertRow(db, new FilterHandle(TEST_FILTER, "dummy1",TEST_RULE_IN_FILTER, "dummy2"));
        db.close();
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilterRules.class);
        myActivityRule.getActivity().startActivity(intent); //relaunch
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterRuleProvider.DeleteFilterRule(db, myRuleId);
        FilterRuleProvider.DeleteFilterRule(db, TEST_RULE_IN_FILTER);
        FilterProvider.DeleteFilter(db, TEST_FILTER);
        db.close();
    }

    @Test
    public void TestSelectFromListOfExisting() throws Throwable {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).check(matches(not(isEnabled())));
        Activity currentActivity = InstrumentTestHelper.getCurrentActivity();
        assertEquals("Enetered the NewEditCalendarRule activity", NewEditFilterRule.class, currentActivity.getClass());
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
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).check(matches(not((isEnabled()))));
        //click on the change action to edit the rule
        onView(ViewMatchers.withId(R.id.action_change)).perform(click());
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).check(matches(isEnabled()));
        //and that the delete button has disappeared
        onView(ViewMatchers.withId(R.id.action_delete)).check(doesNotExist());
        //and that there is no save action  (no changes yet)
        onView(ViewMatchers.withId(R.id.action_save)).check(doesNotExist());
        //now change the description
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).perform(replaceText("axaxa"));
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
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).perform(replaceText("axaxa"));
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

    @Test
    public void TestAutoSaveAfterPatternsUpdate() {
        onView(ViewMatchers.withText(TEST_RULE)).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(TEST_RULE)).perform(click());
        onView(ViewMatchers.withId(R.id.action_change)).perform(click());
        onView(ViewMatchers.withId(R.id.bt_filter_rule_patterns)).perform(click());
        Activity activity = InstrumentTestHelper.getCurrentActivity();
        assertEquals("EditFilterPatterns activity", EditFilterPatterns.class.getCanonicalName(), activity.getClass().getCanonicalName());
        openActionBarOverflowOrOptionsMenu(activity);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("123456"));
        onView(withText("OK")).perform(click());
        onView(withText("123456")).check(matches(isDisplayed()));
        onView(withId(R.id.action_update_patterns)).perform(click());
        //now chack that it is saved
        SQLiteDatabase db = new DbHelper(activity).getReadableDatabase();
        FilterRule fr = FilterRuleProvider.FindFilterRule(db,TEST_RULE);
        assertNotNull("The rule is not null",fr);
        assertNotNull("The rule contains the pattern",fr.getPattern("123456"));
    }
}
