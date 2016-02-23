package com.prismaqf.callblocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.prismaqf.callblocker.rules.CalendarRule;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Activity to create or edit a calendar rule
 * @author ConteDiMonteCristo
 */
public class NewEditCalendarRule extends ActionBarActivity {

    private abstract class RuleNameValidator implements TextWatcher {

        private final TextView mySource, myTarget;
        private final ArrayList<String> myUsedNames;

        public RuleNameValidator(TextView source, TextView target, ArrayList<String> names) {
            mySource = source;
            myTarget = target;
            myUsedNames = names;
        }

        public abstract void validate(TextView source, TextView target,  ArrayList<String> names, String text);

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = mySource.getText().toString();
            validate(mySource, myTarget, myUsedNames, text);
        }
    }

    private static final String TAG = NewEditCalendarRule.class.getCanonicalName();
    public static final String ACTION_KEY  = "com.prismaqft.callblocker:key";
    public static final String ACTION_CREATE  = "com.prismaqf.callblocker:create";
    public static final String ACTION_UPDATE  = "com.prismaqf.callblocker:update";
    private CheckBox cb_Monday, cb_Tuesday, cb_Wednesday, cb_Thursday, cb_Friday, cb_Saturday, cb_Sunday;
    private MenuItem mi_save, mi_delete, mi_change;
    private EditText ed_name;
    private TextView tx_validation;
    private Button bn_from, bn_to;
    private CalendarRule myNewRule, myOrigRule;
    private ArrayList<String> myRuleNames;
    private String myAction;

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
        ed_name.clearFocus();
        bn_from = (Button) findViewById(R.id.bt_from_time);
        bn_to = (Button) findViewById(R.id.bt_to_time);
        tx_validation = (TextView)findViewById(R.id.tx_calendar_rule_validation);


        Intent intent = getIntent();
        myRuleNames = intent.getStringArrayListExtra(getString(R.string.ky_calendar_rule_names));


        if (intent.hasExtra(ACTION_KEY) && intent.getStringExtra(ACTION_KEY).equals(ACTION_UPDATE)) {
            myNewRule  = CalendarRule.makeRule(intent.getExtras());
            myOrigRule = CalendarRule.makeRule(intent.getExtras());
            myAction = ACTION_UPDATE;

        } else {
            myNewRule = new CalendarRule(); //always active by default (all days of week and full day)
            myOrigRule = new CalendarRule();
            myAction = ACTION_CREATE;
        }
        refreshWidgets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_calendar_rules, menu);
        mi_save = menu.findItem(R.id.action_save_rule);
        mi_delete = menu.findItem(R.id.action_delete_rule);
        mi_change = menu.findItem(R.id.action_change_rule);
        if (myAction.equals(ACTION_CREATE)) {
            mi_delete.setVisible(false);
            mi_change.setVisible(false);
        }
        else {
            mi_save.setVisible(false);
        }

        //add text validation
        ed_name.addTextChangedListener(new RuleNameValidator(ed_name,tx_validation, myRuleNames) {
            @Override
            public void validate(TextView source, TextView target, ArrayList<String> names, String text) {
                if (source.getText().toString().equals("")) {
                    target.setText(R.string.tx_validation_rule_name_empty);
                    mi_save.setVisible(false);
                    return;
                }
                if (names.contains(source.getText().toString())) {
                    target.setText(R.string.tx_validation_rule_name_used);
                    mi_save.setVisible(false);
                    return;
                }
                mi_save.setVisible(true);
                target.setText(R.string.tx_validation_rule_valid);
                myNewRule.setName(text);
            }
        });
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onAllDays(View view) {
        myNewRule.setDayMask(EnumSet.allOf(CalendarRule.DayOfWeek.class));
        refreshWidgets();
    }

    public void onNoDays(View view) {
        myNewRule.setDayMask(EnumSet.noneOf(CalendarRule.DayOfWeek.class));
        refreshWidgets();
    }

    public void onWorkingDays(View view) {
        myNewRule.getDayMask().addAll(EnumSet.of(CalendarRule.DayOfWeek.MONDAY,
                CalendarRule.DayOfWeek.TUESDAY,
                CalendarRule.DayOfWeek.WEDNESDAY,
                CalendarRule.DayOfWeek.THURSDAY,
                CalendarRule.DayOfWeek.FRIDAY));
        refreshWidgets();
    }

    public void onWeekEnd(View view) {
        myNewRule.getDayMask().addAll(EnumSet.of(CalendarRule.DayOfWeek.SATURDAY,
                                                 CalendarRule.DayOfWeek.SUNDAY));
        refreshWidgets();
    }

    public void onFromTime(View view) {
    }

    public void onToTime(View view) {
    }

    private void refreshWidgets() {
        ed_name.setText(myNewRule.getName());
        cb_Monday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.MONDAY));
        cb_Tuesday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.TUESDAY));
        cb_Wednesday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.WEDNESDAY));
        cb_Thursday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.THURSDAY));
        cb_Friday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.FRIDAY));
        cb_Saturday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.SATURDAY));
        cb_Sunday.setChecked(myNewRule.getDayMask().contains(CalendarRule.DayOfWeek.SUNDAY));
        bn_from.setText(myNewRule.getStartTime());
        bn_to.setText(myNewRule.getEndTime());
    }

    public String getMyAction() {
        return myAction;
    }
}
