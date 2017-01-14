package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.LoggedCallProvider;
import com.prismaqf.callblocker.utils.CountingMatcher;
import com.prismaqf.callblocker.utils.DebugDBFileName;
import com.prismaqf.callblocker.utils.InstrumentTestHelper;
import com.prismaqf.callblocker.utils.PatternAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class ManageFilterPatternsTest{

    private long n1,n2;

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @Rule
    public final ActivityTestRule<NewEditFilterRule> myActivityRule = new ActivityTestRule(NewEditFilterRule.class);

    @Before
    public void before() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        db.delete(DbContract.FilterRules.TABLE_NAME, null, null);
        db.delete(DbContract.FilterPatterns.TABLE_NAME, null, null);
        db.delete(DbContract.LoggedCalls.TABLE_NAME,null,null);
        LoggedCallProvider.LoggedCall lc1 = new LoggedCallProvider.LoggedCall(1,null,"123","dummy1");
        LoggedCallProvider.LoggedCall lc2 = new LoggedCallProvider.LoggedCall(2,null,"456","dummy2");
        n1 = LoggedCallProvider.InsertRow(db,lc1);
        n2 = LoggedCallProvider.InsertRow(db,lc2);
        db.close();
        onView(withId(R.id.bt_filter_rule_patterns)).check(matches(isDisplayed()));
        onView(withId(R.id.bt_filter_rule_patterns)).perform(click());
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        LoggedCallProvider.DeleteLoggedCall(db, n1);
        LoggedCallProvider.DeleteLoggedCall(db, n2);
        db.close();
    }

    @Test
    public void SmokeTest() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
    }

    @Test
    public void PickFromLog() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        //make sure we land on the log list
        onView(ViewMatchers.withText("dummy1")).check(matches(isDisplayed()));
        //choose one entry from the log
        onView(ViewMatchers.withText("dummy1")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        final CountingMatcher matcher = new CountingMatcher("123");
        //and here the check
        onData(matcher).check(matches(anything()));
        assertEquals("Number of counts", 1, matcher.getCount());
    }

    @Test
    public void PickFromLogRepeatedNumber() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy1")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy1")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        //second time over
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy1")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy1")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        //now make sure it is only counted once
        final CountingMatcher matcher = new CountingMatcher("123");
        //and here the check
        onData(matcher).check(matches(anything()));
        assertEquals("Number of counts", 1, matcher.getCount());
    }

    @Test
    public void PickFromLogWithScreenRotation() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy1")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy1")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        //second time over
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy2")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy2")).perform(click());
        onView(withText("456")).check(matches(isDisplayed()));
        //Counting items
        final CountingMatcher matcher = new CountingMatcher("*");
        onData(matcher).atPosition(0).check(matches(anything()));
        assertEquals("Number of counts", 2, matcher.getCount());
        //now rotate
        Activity ca = InstrumentTestHelper.getCurrentActivity();
        InstrumentTestHelper.rotateScreen(ca);
        matcher.resetCount();
        onData(matcher).atPosition(0).check(matches(anything()));
        assertEquals("Number of counts", 2, matcher.getCount());
    }

    @Test
    public void TestCheckingPersistsWithRotation() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy1")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy1")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        //second time over
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy2")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy2")).perform(click());
        onView(withText("456")).check(matches(isDisplayed()));
        //perform checking 456
        onData(containsString("123")).onChildView(withId(R.id.cb_pattern)).check(matches(isNotChecked()));
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).check(matches(isNotChecked()));
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).perform(click());
        onData(containsString("123")).onChildView(withId(R.id.cb_pattern)).check(matches(isNotChecked()));
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).check(matches(isChecked()));
        //now rotate
        Activity ca = InstrumentTestHelper.getCurrentActivity();
        InstrumentTestHelper.rotateScreen(ca);
        onData(containsString("123")).onChildView(withId(R.id.cb_pattern)).check(matches(isNotChecked()));
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).check(matches(isChecked()));
    }

    @Test
    public void TestDeleteChecked() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy1")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy1")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        //second time over
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Pick from log")).perform(click());
        onView(ViewMatchers.withText("dummy2")).check(matches(isDisplayed()));
        onView(ViewMatchers.withText("dummy2")).perform(click());
        onView(withText("456")).check(matches(isDisplayed()));
        //perform checking 456
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).perform(click());
        //now delete checked
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Delete selected patterns")).perform(click());
        //Counting items
        final CountingMatcher matcher = new CountingMatcher("123");
        onData(matcher).atPosition(0).check(matches(anything()));
        assertEquals("Number of counts", 1, matcher.getCount());
        onView(withText("456")).check(doesNotExist());
    }


    @Test
    public void TestAddPattern() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("123*456"));
        onView(withText("OK")).perform(click());
        onView(withText("123*456")).check(matches(isDisplayed()));
    }

    @Test
    public void TestAddPatternWithExtraChars() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("1-23*4+5)6"));
        onView(withText("OK")).perform(click());
        onView(withText("123*456")).check(matches(isDisplayed()));
    }

    @Test
    public void TestUpdateActionValidation() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first does not exists
        onView(withId(R.id.action_update_patterns)).check(doesNotExist());
        //now modify the rule
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("1-23*4+5)6"));
        onView(withText("OK")).perform(click());
        onView(withText("123*456")).check(matches(isDisplayed()));
        //the update action should be available now
        onView(withId(R.id.action_update_patterns)).check(matches(isDisplayed()));
        //also upn rotation
        InstrumentTestHelper.rotateScreen(InstrumentTestHelper.getCurrentActivity());
        onView(withId(R.id.action_update_patterns)).check(matches(isDisplayed()));
        //now remove the change
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).perform(click());
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Delete selected patterns")).perform(click());
        //now the update action should disappear
        onView(withId(R.id.action_update_patterns)).check(doesNotExist());
    }

    @Test
    public void TestDeleteActionValidation() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first does not exists
        //now modify the rule
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("1-23*4+5)6"));
        onView(withText("OK")).perform(click());
        onView(withText("123*456")).check(matches(isDisplayed()));
        //the still unchecked, the delete action should be disables
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Delete selected patterns")).check(doesNotExist());
        //now check it
        pressBack();
        onData(containsString("123")).onChildView(withId(R.id.cb_pattern)).perform(click());
        //the delete should now be anabled
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Delete selected patterns")).check(matches(isDisplayed()));
        onView(withText("Delete selected patterns")).perform(click());
    }

    @Test
    public void TestUpdate() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        //first time
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("123*456"));
        onView(withText("OK")).perform(click());
        onView(withText("123*456")).check(matches(isDisplayed()));
        //now update
        onView(withId(R.id.action_update_patterns)).perform(click());
        //we should have landed in the parent activity and the rule description should be unpdates
        onView(withId(R.id.tx_rule_description)).check(matches(withText(containsString("123"))));

    }

    @Test
    public void PatternsOrderedAlphabetically() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("123"));
        onView(withText("OK")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("456"));
        onView(withText("OK")).perform(click());
        onView(withText("456")).check(matches(isDisplayed()));
        //now get the ListView and the adapter
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).onChildView(withId(R.id.text_pattern)).check(matches(withText(containsString("123"))));
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(1).onChildView(withId(R.id.text_pattern)).check(matches(withText(containsString("456"))));
        //now add a pattern which should ordered in between the last two
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("125"));
        onView(withText("OK")).perform(click());
        onView(withText("125")).check(matches(isDisplayed()));
        //and check that the order is respected
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).onChildView(withId(R.id.text_pattern)).check(matches(withText(containsString("123"))));
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(1).onChildView(withId(R.id.text_pattern)).check(matches(withText(containsString("125"))));
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(2).onChildView(withId(R.id.text_pattern)).check(matches(withText(containsString("456"))));
    }

    @Test
    public void EditAPatternShouldWork() {
        onView(withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
        Context ctx = InstrumentationRegistry.getTargetContext();
        openActionBarOverflowOrOptionsMenu(ctx);
        //add two patterns
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("123"));
        onView(withText("OK")).perform(click());
        onView(withText("123")).check(matches(isDisplayed()));
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("456"));
        onView(withText("OK")).perform(click());
        onView(withText("456")).check(matches(isDisplayed()));
        //first the edit is not displayed because nothing is checked
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Edit a single pattern")).check(doesNotExist());
        pressBack();
        //now tick one box
        onData(containsString("123")).onChildView(withId(R.id.cb_pattern)).perform(click());
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Edit a single pattern")).check(matches(isDisplayed()));
        pressBack();
        //now tick another box
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).perform(click());
        openActionBarOverflowOrOptionsMenu(ctx);
        //the edit option should have disappeared
        onView(withText("Edit a single pattern")).check(doesNotExist());
        pressBack();
        //now untick the second box
        onData(containsString("456")).onChildView(withId(R.id.cb_pattern)).perform(click());
        //and edit the first
        openActionBarOverflowOrOptionsMenu(ctx);
        onView(withText("Edit a single pattern")).check(matches(isDisplayed()));
        onView(withText("Edit a single pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).check(matches(withText("123")));
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(replaceText("999"));
        onView(withText("OK")).perform(click());
        //now check that the text has changed
        onView(withText("999")).check(matches(isDisplayed()));
    }

}
