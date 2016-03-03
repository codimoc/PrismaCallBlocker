package com.prismaqf.callblocker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.DbHelperTest;
import com.prismaqf.callblocker.sql.FilterRuleProvider;
import com.prismaqf.callblocker.utils.DebugHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EditFilterRuleTest extends DebugHelper{

    static {
        DbHelper.SetDebugDb(myKey, DbHelperTest.DB_NAME);
    }

    private static final String TEST_RULE_NAME = "Test rule";

    @Rule
    public final ActivityTestRule<CallBlockerManager> myActivityRule = new ActivityTestRule(CallBlockerManager.class);

    @Before
    public void before() {
        FilterRule fr = new FilterRule(TEST_RULE_NAME, "Rule used for testing");
        fr.addPattern("123");
        fr.addPattern("4*56");
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterRuleProvider.InsertRow(db,fr);
        db.close();
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterRuleProvider.DeleteFilterRule(db, TEST_RULE_NAME);
        db.close();
    }

    @Test
    public void SmokeTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilterRules.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the rule is displayed
        onView(ViewMatchers.withId(R.id.text_rule_name)).check(matches(withText(TEST_RULE_NAME)));
        //test that the add action is present
        onView(ViewMatchers.withId(R.id.action_new_item)).check(matches(isDisplayed()));
    }

}
