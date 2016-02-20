package com.prismaqf.callblocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.prismaqf.callblocker.rules.CalendarRule;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Activity to create or edit a calendar rule
 * @author ConteDiMonteCristo
 */
public class NewEditCalendarRule extends ActionBarActivity {

    private static final String TAG = NewEditCalendarRule.class.getCanonicalName();
    public static final String ACTION_KEY  = "com.prismaqf.callblocker:key";
    public static final String ACTION_CREATE  = "com.prismaqf.callblocker:create";
    public static final String ACTION_UPDATE  = "com.prismaqf.callblocker:update";
    private CheckBox cb_Monday, cb_Tuesday, cb_Wednesday, cb_Thursday, cb_Friday, cb_Saturday, cb_Sunday;
    private EditText ed_name;
    private CalendarRule myNewRule, myOrigRule;
    private ArrayList<String> myRuleNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_edit);

        cb_Monday = (CheckBox) findViewById(R.id.cb_Monday);
        cb_Tuesday = (CheckBox) findViewById(R.id.cb_Tuesday);
        cb_Wednesday = (CheckBox) findViewById(R.id.cb_Wednesday);
        cb_Thursday = (CheckBox) findViewById(R.id.cb_Thursday);
        cb_Friday = (CheckBox) findViewById(R.id.cb_Friday);
        cb_Saturday = (CheckBox) findViewById(R.id.cb_Saturday);
        cb_Sunday = (CheckBox) findViewById(R.id.cb_Sunday);
        ed_name = (EditText) findViewById(R.id.edit_calendar_rule_name);
        //put all cb to unchecked
        onAllDays(null);

        Intent intent = getIntent();
        myRuleNames = intent.getStringArrayListExtra(getString(R.string.ky_calendar_rule_names));
        if (intent.hasExtra(ACTION_KEY) && intent.getStringExtra(ACTION_KEY).equals(ACTION_UPDATE)) {
            myNewRule  = CalendarRule.makeRule(intent.getExtras());
            myOrigRule = CalendarRule.makeRule(intent.getExtras());

        } else {
            myNewRule = new CalendarRule(); //always active by default (all days of week and full day)
            myOrigRule = new CalendarRule();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_call_blocker_manager, menu);
        return true;
    }

    public void onAllDays(View view) {
        cb_Monday.setChecked(true);
        cb_Tuesday.setChecked(true);
        cb_Wednesday.setChecked(true);
        cb_Thursday.setChecked(true);
        cb_Friday.setChecked(true);
        cb_Saturday.setChecked(true);
        cb_Sunday.setChecked(true);
    }

    public void onNoDays(View view) {
        cb_Monday.setChecked(false);
        cb_Tuesday.setChecked(false);
        cb_Wednesday.setChecked(false);
        cb_Thursday.setChecked(false);
        cb_Friday.setChecked(false);
        cb_Saturday.setChecked(false);
        cb_Sunday.setChecked(false);
    }

    public void onWorkingDays(View view) {
        cb_Monday.setChecked(true);
        cb_Tuesday.setChecked(true);
        cb_Wednesday.setChecked(true);
        cb_Thursday.setChecked(true);
        cb_Friday.setChecked(true);
    }

    public void onWeekEnd(View view) {
        cb_Saturday.setChecked(true);
        cb_Sunday.setChecked(true);
    }

    public void onFromTime(View view) {
    }

    public void onToTime(View view) {
    }
}
