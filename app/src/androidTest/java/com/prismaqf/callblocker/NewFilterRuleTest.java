package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.action.ReplaceTextAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.prismaqf.callblocker.utils.DebugDBFileName;
import com.prismaqf.callblocker.utils.InstrumentTestHelper;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class NewFilterRuleTest {

    private Intent intent;
    private Context ctx;

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @Rule
    public final ActivityTestRule<EditFilterRules> myActivityRule = new ActivityTestRule<>(EditFilterRules.class);

    @Before
    public void before() {
        ctx = myActivityRule.getActivity();
        intent = new Intent(ctx,NewEditFilterRule.class);
        intent.putExtra(NewEditActivity.KEY_ACTION, NewEditActivity.ACTION_CREATE);
    }

    @Test
    public void TestActionOnCreating() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).check(matches(isEnabled()));
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).check(matches(isEnabled()));
        onView(ViewMatchers.withId(R.id.bt_filter_rule_patterns)).check(matches(isEnabled()));
        onView(ViewMatchers.withId(R.id.tx_filter_rule_validation)).check(matches(isEnabled()));
        onView(ViewMatchers.withId(R.id.tx_rule_description)).check(matches(isEnabled()));
    }

    @Test
    public void TestEmptyNameShouldFlag() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.tx_filter_rule_validation)).check(matches(withText(containsString("can not be empty"))));
    }

    @Test
    public void TestUsedNameShouldFlag() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES,
                new ArrayList<>(Collections.singletonList("first")));
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).perform(new ReplaceTextAction("first"));
        onView(ViewMatchers.withId(R.id.tx_filter_rule_validation)).check(matches(withText(containsString("name already used"))));
    }

    @Test
    public void TestSaveAction() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_save)).check(matches(isDisplayed()));
        //now empty the rule name, the action disappears
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).perform(new ReplaceTextAction(""));
        onView(ViewMatchers.withId(R.id.action_save)).check(doesNotExist());
        //reinsert a valid name, the action is back
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).perform(new ReplaceTextAction("a"));
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
    public void TestButtonFilterPatterns() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.bt_filter_rule_patterns)).check(matches(isEnabled()));
        onView(ViewMatchers.withId(R.id.bt_filter_rule_patterns)).perform(click());
        onView(ViewMatchers.withId(R.id.action_help_patterns)).check(matches(isDisplayed()));
    }

    @Test
    public void TestHelpAction() {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.action_help)).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.action_help)).perform(click());
        onView(ViewMatchers.withText("Help on editing filter rules")).check(matches(isDisplayed()));
    }

    @Test
    public void TestDynamicStateOnRotation() throws Throwable {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).perform(new ReplaceTextAction("Name"));
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).perform(new ReplaceTextAction("Description"));
        onView(ViewMatchers.withId(R.id.bt_filter_rule_patterns)).perform(click());
        Activity activity = InstrumentTestHelper.getCurrentActivity();
        openActionBarOverflowOrOptionsMenu(activity);
        onView(withText("Add a pattern")).perform(click());
        onView(withClassName(equalTo(EditText.class.getCanonicalName()))).perform(typeText("123*456"));
        onView(withText("OK")).perform(click());
        onView(withText("123*456")).check(matches(isDisplayed()));
        onView(withId(R.id.action_update_patterns)).perform(click());
        onView(withId(R.id.tx_rule_description)).check(matches(withText(containsString("123*456"))));
        activity = InstrumentTestHelper.getCurrentActivity();
        InstrumentTestHelper.rotateScreen(activity);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).check(matches(withText("Name")));
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).check(matches(withText("Description")));
        onView(withId(R.id.tx_rule_description)).check(matches(withText(containsString("123*456"))));
        InstrumentTestHelper.rotateScreen(activity);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).check(matches(withText("Name")));
        onView(ViewMatchers.withId(R.id.edit_filter_rule_description)).check(matches(withText("Description")));
        onView(withId(R.id.tx_rule_description)).check(matches(withText(containsString("123*456"))));
    }

    @Test
    public void TestRotationWithEmptyText() throws Throwable {
        intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, new ArrayList<String>());
        ctx.startActivity(intent);
        Activity activity = InstrumentTestHelper.getCurrentActivity();
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).perform(new ReplaceTextAction(""));
        InstrumentTestHelper.rotateScreen(activity);
        onView(ViewMatchers.withId(R.id.edit_filter_rule_name)).check(matches(withText("")));
    }
}
