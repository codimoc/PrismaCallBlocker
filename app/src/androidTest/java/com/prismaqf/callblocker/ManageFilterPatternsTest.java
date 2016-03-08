package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.base.Predicate;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.DbHelperTest;
import com.prismaqf.callblocker.sql.LoggedCallProvider;
import com.prismaqf.callblocker.utils.ConditionMatcher;
import com.prismaqf.callblocker.utils.CountingMatcher;
import com.prismaqf.callblocker.utils.DebugHelper;

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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

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
        final Predicate predicate = new Predicate() {

            @Override
            public boolean apply(Object o) {
                return matcher.getCount()==1;
            }
        };
        //and here the check
        onData(matcher).check(matches(new ConditionMatcher(predicate,"matcher count == 1")));
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
        final Predicate predicate = new Predicate() {

            @Override
            public boolean apply(Object o) {
                return matcher.getCount()==1;
            }
        };
        //and here the check
        onData(matcher).check(matches(new ConditionMatcher(predicate,"matcher count == 1")));
    }

}
