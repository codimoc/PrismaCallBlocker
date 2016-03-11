package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.support.test.espresso.action.ReplaceTextAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.utils.DebugDBFileName;
import com.prismaqf.callblocker.utils.InstrumentTestHelper;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

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
public class NewCalendarRuleTest {

    private Intent intent;
    private Context ctx;

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();


    @Rule
    public final ActivityTestRule<EditCalendarRules> myActivityRule = new ActivityTestRule<>(EditCalendarRules.class);

    @Before
    public void before() {
        ctx = myActivityRule.getActivity();
        intent = new Intent(ctx,NewEditCalendarRule.class);
        intent.putExtra(NewEditActivity.ACTION_KEY, NewEditActivity.ACTION_CREATE);
    }

    @Test
    public void TestActionOnCreating() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
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
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.tx_calendar_rule_validation)).check(matches(withText(containsString("can not be empty"))));
    }

    @Test
    public void TestUsedNameShouldFlag() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES,
                new ArrayList<>(Collections.singletonList("first")));
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("first"));
        onView(ViewMatchers.withId(R.id.tx_calendar_rule_validation)).check(matches(withText(containsString("name already used"))));
    }

    @Test
    public void TestSaveAction() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_save)).check(matches(isDisplayed()));
        //now empty the rule name, the action disappears
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.action_save)).check(doesNotExist());
        //reinsert a valid name, the action is back
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("a"));
        onView(ViewMatchers.withId(R.id.action_save)).check(matches(isDisplayed()));
    }

    @Test
    public void TestDeleteActionMissingOnCreate(){
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_delete)).check(doesNotExist());
    }

    @Test
    public void TestChangeActionMissingOnCreate(){
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_change)).check(doesNotExist());
    }

    @Test
    public void TestNoDays() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
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
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
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
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        Activity activity = InstrumentTestHelper.getCurrentActivity();
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isChecked()));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).perform(click());
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("dummy"));
        InstrumentTestHelper.rotateScreen(activity);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).check(matches(withText("dummy")));
        onView(ViewMatchers.withId(R.id.cb_Wednesday)).check(matches(isNotChecked()));
        InstrumentTestHelper.rotateScreen(activity);
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
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
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
}
