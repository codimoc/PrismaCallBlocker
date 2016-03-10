package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.WindowManager;
import android.widget.EditText;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.DbHelperTest;
import com.prismaqf.callblocker.sql.LoggedCallProvider;
import com.prismaqf.callblocker.utils.CountingMatcher;
import com.prismaqf.callblocker.utils.DebugHelper;
import com.prismaqf.callblocker.utils.InstrumentTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
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
import static org.hamcrest.CoreMatchers.instanceOf;

@RunWith(AndroidJUnit4.class)
public class ManageFilterPatternsTest extends DebugHelper{

    static {
        DbHelper.SetDebugDb(myKey, DbHelperTest.DB_NAME);
    }

    private long n1,n2;

    @Rule
    public final ActivityTestRule<NewEditFilterRule> myActivityRule = new ActivityTestRule(NewEditFilterRule.class);

    @Before
    public void before() {

        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        LoggedCallProvider.LoggedCall lc1 = new LoggedCallProvider.LoggedCall(1,-1,"123","dummy1");
        LoggedCallProvider.LoggedCall lc2 = new LoggedCallProvider.LoggedCall(2,-1,"456","dummy2");
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

}
