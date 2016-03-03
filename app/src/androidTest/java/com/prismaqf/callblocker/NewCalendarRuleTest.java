package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Parcel;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ReplaceTextAction;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.DbHelperTest;
import com.prismaqf.callblocker.utils.DebugHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NewCalendarRuleTest extends DebugHelper {

    static {
        DbHelper.SetDebugDb(myKey, DbHelperTest.DB_NAME);
    }

    private Intent intent;
    private Context ctx;

    @Rule
    public final ActivityTestRule<EditCalendarRules> myActivityRule = new ActivityTestRule(EditCalendarRules.class);

    @Before
    public void before() {
        ctx = myActivityRule.getActivity();
        intent = new Intent(ctx,NewEditCalendarRule.class);
        intent.putExtra(NewEditActivity.ACTION_KEY, NewEditActivity.ACTION_CREATE);
    }

    @Test
    public void TestActionOnCreating() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(isEnabled()));
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(withText("always")));
        onView(ViewMatchers.withId(R.id.cb_Monday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Tuesday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Thursday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Friday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Saturday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Sunday)).check(matches(isEnabled())).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.bt_from_time)).check(matches(withText("From 00:00")));
        onView(ViewMatchers.withId(R.id.bt_to_time)).check(matches(withText("To 23:59")));
    }

    @Test
    public void TestEmptyNameShouldFlag() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.tx_calendar_rule_validation)).check(matches(withText(containsString("can not be empty"))));
    }

    @Test
    public void TestUsedNameShouldFlag() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names),
                new ArrayList<>(Arrays.asList("first")));
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("first"));
        onView(ViewMatchers.withId(R.id.tx_calendar_rule_validation)).check(matches(withText(containsString("name already used"))));
    }

    @Test
    public void TestSaveAction() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(matches(isDisplayed()));
        //now empty the rule name, the action disappears
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(doesNotExist());
        //reinsert a valid name, the action is back
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("a"));
        onView(ViewMatchers.withId(R.id.action_save_rule)).check(matches(isDisplayed()));
    }

    @Test
    public void TestDeleteActionMissingOnCreate(){
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_delete_rule)).check(doesNotExist());
    }

    @Test
    public void TestChangeActionMissingOnCreate(){
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_change_rule)).check(doesNotExist());
    }

    @Test
    public void TestNoDays() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Monday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Tuesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Thursday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Friday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Saturday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Sunday)).check(matches(isNotChecked()));
    }

    @Test
    public void TestAllDays() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        onView(ViewMatchers.withId(R.id.bt_all_days)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Monday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Tuesday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Thursday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Friday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Saturday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Sunday)).check(matches(isChecked()));
    }

    @Test
    public void TestParcelable() {
        CalendarRule c1 = new CalendarRule("first", EnumSet.of(CalendarRule.DayOfWeek.MONDAY, CalendarRule.DayOfWeek.FRIDAY),2,15,10,7);
        Parcel parcel = Parcel.obtain();
        c1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        CalendarRule c2 = CalendarRule.CREATOR.createFromParcel(parcel);
        assertEquals("Equal after Prcelization", c1, c2);
    }

    @Test
    public void TestDynamicStateOnRotation() throws Throwable {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        Activity activity = getCurrentActivity();
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("dummy"));
        rotateScreen(activity);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(withText("dummy")));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        rotateScreen(activity);
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(withText("dummy")));
    }

//      todo: when the text is not valid rotation does not preserve. Need maybe some investigation
//    @Test
//    public void TestRotationWithEmptyText() throws Throwable {
//        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
//        ctx.startActivity(intent);
//        Activity activity = getCurrentActivity();
//        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction(""));;
//        rotateScreen(activity);
//        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(withText("")));;
//    }

    @Test
    public void TestSingleDays() {
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.bt_no_days)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Monday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Monday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Monday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Tuesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Tuesday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Tuesday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Thursday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Thursday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Thursday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Friday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Friday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Friday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Saturday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Saturday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Saturday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Sunday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.cb_Sunday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Sunday)).check(matches(isChecked()));
    }

    private void rotateScreen(Activity currentActivity) {
        Context context = InstrumentationRegistry.getTargetContext();
        int orientation = context.getResources().getConfiguration().orientation;
        currentActivity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
