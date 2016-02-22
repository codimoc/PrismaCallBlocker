package com.prismaqf.callblocker;

import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.action.ReplaceTextAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class NewEditCalendarRuleTest {

    @Rule
    public final ActivityTestRule<EditCalendarRules> myActivityRule = new ActivityTestRule(EditCalendarRules.class);

    @Test
    public void TestActionOnCreating() {
        Context ctx = myActivityRule.getActivity();
        Intent intent = new Intent(ctx,NewEditCalendarRule.class);
        intent.putExtra(NewEditCalendarRule.ACTION_KEY,NewEditCalendarRule.ACTION_CREATE);
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
        Context ctx = myActivityRule.getActivity();
        Intent intent = new Intent(ctx,NewEditCalendarRule.class);
        intent.putExtra(NewEditCalendarRule.ACTION_KEY, NewEditCalendarRule.ACTION_CREATE);
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names), new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.tx_calendar_rule_validation)).check(matches(withText(containsString("can not be empty"))));
    }

    @Test
    public void TestUsedNameShouldFlag() {
        Context ctx = myActivityRule.getActivity();
        Intent intent = new Intent(ctx,NewEditCalendarRule.class);
        intent.putExtra(NewEditCalendarRule.ACTION_KEY,NewEditCalendarRule.ACTION_CREATE);
        intent.putStringArrayListExtra(ctx.getString(R.string.ky_calendar_rule_names),
                                       new ArrayList<>(Arrays.asList("first")));
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_calendar_rule_name)).perform(new ReplaceTextAction("first"));
        onView(ViewMatchers.withId(R.id.tx_calendar_rule_validation)).check(matches(withText(containsString("name already used"))));
    }

    @Test
    public void TestSaveAction() {
        Context ctx = myActivityRule.getActivity();
        Intent intent = new Intent(ctx, NewEditCalendarRule.class);
        intent.putExtra(NewEditCalendarRule.ACTION_KEY, NewEditCalendarRule.ACTION_CREATE);
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
}
