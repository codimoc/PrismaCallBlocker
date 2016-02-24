package com.prismaqf.callblocker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.sql.DbHelper;

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

    @SuppressLint("ValidFragment")
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        CalendarRule rule;
        String startEnd;
        NewEditCalendarRule parent;


        public TimePickerFragment(CalendarRule rule, String startEnd, NewEditCalendarRule parent) {
            this.rule = rule;
            this.startEnd = startEnd;
            this.parent = parent;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour, minute;

            if (rule != null && startEnd!=null) {
                if (startEnd.toLowerCase().equals("start")) {
                    hour = rule.getStartHour();
                    minute = rule.getStartMin();
                } else {
                    hour = rule.getEndHour();
                    minute = rule.getEndMin();
                }
                return new TimePickerDialog(getActivity(), this, hour, minute,
                        DateFormat.is24HourFormat(getActivity()));
            }
            Log.e(TAG,"Can't initialise a TimePicker");
            return new TimePickerDialog(getActivity(),this,0,0,DateFormat.is24HourFormat(getActivity()));
        }


        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            if (rule != null && startEnd != null)
                if (startEnd.toLowerCase().equals("start")) {
                    rule.setStartHour(timePicker.getCurrentHour());
                    rule.setStartMin(timePicker.getCurrentMinute());
                } else {
                    rule.setEndHour(timePicker.getCurrentHour());
                    rule.setEndMin(timePicker.getCurrentMinute());
                }
            else Log.e(TAG, "Can't set time from TimePicker");

            parent.refreshWidgets(true);
        }
    }

    private class SaveOperation extends AsyncTask<CalendarRule, Void, Void> {

        @Override
        protected Void doInBackground(CalendarRule... rules) {
            SQLiteDatabase db = new DbHelper(NewEditCalendarRule.this).getWritableDatabase();
            CalendarRule rule = rules[0];
            try {
                com.prismaqf.callblocker.sql.CalendarRule.InsertRow(db, rule.getName(), rule.getBinaryMask(), rule.getBareStartTime(), rule.getBareEndTime());
            }
            finally {
                db.close();    
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void v) {
            Intent intent = new Intent(NewEditCalendarRule.this, EditCalendarRules.class);
            startActivity(intent);
        }
    }



    private static final String TAG = NewEditCalendarRule.class.getCanonicalName();
    public static final String ACTION_KEY  = "com.prismaqft.callblocker:key";
    public static final String KEY_NEW  = "com.prismaqft.callblocker:keynew";
    public static final String KEY_ORIG  = "com.prismaqft.callblocker:keyorig";
    public static final String KEY_ISNAMEVALID  = "com.prismaqft.callblocker:namevalid";
    public static final String KEY_RULENAMES  = "com.prismaqft.callblocker:rulenames";
    public static final String KEY_PTRULE  = "com.prismaqft.callblocker:ptrule";
    public static final String ACTION_CREATE  = "com.prismaqf.callblocker:create";
    public static final String ACTION_UPDATE  = "com.prismaqf.callblocker:update";
    private CheckBox cb_Monday, cb_Tuesday, cb_Wednesday, cb_Thursday, cb_Friday, cb_Saturday, cb_Sunday;
    private MenuItem mi_save, mi_delete, mi_change;
    private EditText ed_name;
    private TextView tx_validation;
    private Button bn_from, bn_to, bn_alldays, bn_nodays, bn_workdays, bn_we;
    private CalendarRule myNewRule, myOrigRule, ptRule;  //ptRule is an alias to the active rule
    private ArrayList<String> myRuleNames;
    private String myAction;
    private boolean isNameValid = true;

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
        bn_alldays = (Button) findViewById(R.id.bt_all_days);
        bn_nodays = (Button) findViewById(R.id.bt_no_days);
        bn_workdays = (Button) findViewById(R.id.bt_working_days);
        bn_we = (Button) findViewById(R.id.bt_week_end);
        tx_validation = (TextView)findViewById(R.id.tx_calendar_rule_validation);


        Intent intent = getIntent();
        myRuleNames = intent.getStringArrayListExtra(getString(R.string.ky_calendar_rule_names));


        if (intent.hasExtra(ACTION_KEY) && intent.getStringExtra(ACTION_KEY).equals(ACTION_UPDATE)) {
            myOrigRule = intent.getParcelableExtra(KEY_ORIG);
            try {
                myNewRule  = (CalendarRule)myOrigRule.clone();
            } catch (CloneNotSupportedException e) {
                Log.e(TAG, "Could not clone original rule");
                myNewRule =  CalendarRule.makeRule(intent.getExtras());
            }
            ptRule = myOrigRule;
            myAction = ACTION_UPDATE;

            enableWidgets(false,false);

        } else {
            myNewRule = new CalendarRule(); //always active by default (all days of week and full day)
            myOrigRule = null;
            ptRule = myNewRule;
            myAction = ACTION_CREATE;

            enableWidgets(true,true);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_NEW, myNewRule);
        savedInstanceState.putParcelable(KEY_ORIG, myOrigRule);
        savedInstanceState.putString(ACTION_KEY, myAction);
        savedInstanceState.putBoolean(KEY_ISNAMEVALID, isNameValid);
        savedInstanceState.putStringArrayList(KEY_RULENAMES, myRuleNames);
        savedInstanceState.putString(KEY_PTRULE, ptRule == myOrigRule ? "Original" : "New");

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myNewRule = savedInstanceState.getParcelable(KEY_NEW);
        myOrigRule = savedInstanceState.getParcelable(KEY_ORIG);
        myAction = savedInstanceState.getString(ACTION_KEY);
        isNameValid = savedInstanceState.getBoolean(KEY_ISNAMEVALID);
        myRuleNames = savedInstanceState.getStringArrayList(KEY_RULENAMES);
        String rule = savedInstanceState.getString(KEY_PTRULE,"");
        if (rule.equals("Original"))
            ptRule = myOrigRule;
        else
            ptRule = myNewRule;
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
            ed_name.addTextChangedListener(new RuleNameValidator(ed_name, tx_validation, myRuleNames) {
                @Override
                public void validate(TextView source, TextView target, ArrayList<String> names, String text) {
                    if (source.getText().toString().equals("")) {
                        target.setText(R.string.tx_validation_rule_name_empty);
                        mi_save.setVisible(false);
                        isNameValid = false;
                        return;
                    }
                    if (names!= null && names.contains(source.getText().toString())) {
                        target.setText(R.string.tx_validation_rule_name_used);
                        mi_save.setVisible(false);
                        isNameValid = false;
                        return;
                    }
                    mi_save.setVisible(true);
                    target.setText(R.string.tx_validation_rule_valid);
                    ptRule.setName(text);
                    isNameValid = true;
                }
            });
            refreshWidgets(true);
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save_rule:
                saveCalendarRule();
                return true;
            case R.id.action_change_rule:
                changeCalendarRule();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAllDays(View view) {
        ptRule.setDayMask(EnumSet.allOf(CalendarRule.DayOfWeek.class));
        refreshWidgets(true);
    }

    public void onNoDays(View view) {
        ptRule.setDayMask(EnumSet.noneOf(CalendarRule.DayOfWeek.class));
        refreshWidgets(true);
    }

    public void onWorkingDays(View view) {
        ptRule.getDayMask().addAll(EnumSet.of(CalendarRule.DayOfWeek.MONDAY,
                CalendarRule.DayOfWeek.TUESDAY,
                CalendarRule.DayOfWeek.WEDNESDAY,
                CalendarRule.DayOfWeek.THURSDAY,
                CalendarRule.DayOfWeek.FRIDAY));
        refreshWidgets(true);
    }

    public void onWeekEnd(View view) {
        ptRule.getDayMask().addAll(EnumSet.of(CalendarRule.DayOfWeek.SATURDAY,
                                                 CalendarRule.DayOfWeek.SUNDAY));
        refreshWidgets(true);
    }

    public void onFromTime(final View view) {
        DialogFragment newFragment = new TimePickerFragment(ptRule,"Start",this);
        newFragment.show(getSupportFragmentManager(), "From Time");
    }

    public void onToTime(View view) {
        DialogFragment newFragment = new TimePickerFragment(ptRule,"End",this);
        newFragment.show(getSupportFragmentManager(), "To Time");
    }

    public void onCheckDay(View view) {
        switch (view.getId()) {
            case R.id.cb_Monday:
                if (cb_Monday.isChecked())
                    ptRule.getDayMask().add(CalendarRule.DayOfWeek.MONDAY);
                else
                    ptRule.getDayMask().remove(CalendarRule.DayOfWeek.MONDAY);
                break;
            case R.id.cb_Tuesday:
                if (cb_Tuesday.isChecked())
                    ptRule.getDayMask().add(CalendarRule.DayOfWeek.TUESDAY);
                else
                    ptRule.getDayMask().remove(CalendarRule.DayOfWeek.TUESDAY);
                break;
            case R.id.cb_Wednesday:
                if (cb_Wednesday.isChecked())
                    ptRule.getDayMask().add(CalendarRule.DayOfWeek.WEDNESDAY);
                else
                    ptRule.getDayMask().remove(CalendarRule.DayOfWeek.WEDNESDAY);
                break;
            case R.id.cb_Thursday:
                if (cb_Thursday.isChecked())
                    ptRule.getDayMask().add(CalendarRule.DayOfWeek.THURSDAY);
                else
                    ptRule.getDayMask().remove(CalendarRule.DayOfWeek.THURSDAY);
                break;
            case R.id.cb_Friday:
                if (cb_Friday.isChecked())
                    ptRule.getDayMask().add(CalendarRule.DayOfWeek.FRIDAY);
                else
                    ptRule.getDayMask().remove(CalendarRule.DayOfWeek.FRIDAY);
                break;
            case R.id.cb_Saturday:
            if (cb_Saturday.isChecked())
                ptRule.getDayMask().add(CalendarRule.DayOfWeek.SATURDAY);
            else
                ptRule.getDayMask().remove(CalendarRule.DayOfWeek.SATURDAY);
            break;
            case R.id.cb_Sunday:
                if (cb_Sunday.isChecked())
                    ptRule.getDayMask().add(CalendarRule.DayOfWeek.SUNDAY);
                else
                    ptRule.getDayMask().remove(CalendarRule.DayOfWeek.SUNDAY);
        }
        validateActions();
    }


    private void refreshWidgets(boolean validate) {
        ed_name.setText(ptRule.getName());
        cb_Monday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.MONDAY));
        cb_Tuesday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.TUESDAY));
        cb_Wednesday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.WEDNESDAY));
        cb_Thursday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.THURSDAY));
        cb_Friday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.FRIDAY));
        cb_Saturday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.SATURDAY));
        cb_Sunday.setChecked(ptRule.getDayMask().contains(CalendarRule.DayOfWeek.SUNDAY));
        bn_from.setText(ptRule.getStartTime());
        bn_to.setText(ptRule.getEndTime());
        if (validate) validateActions();
    }

    private void validateActions() {
        mi_save.setVisible(!myNewRule.equals(myOrigRule) && isNameValid);
        mi_delete.setVisible(myAction.equals(ACTION_UPDATE) && ptRule == myOrigRule);
        mi_change.setVisible(myAction.equals(ACTION_UPDATE) && ptRule == myOrigRule);
    }

    private void enableWidgets(boolean nameFlag, boolean widgetFlag) {
        ed_name.setEnabled(nameFlag);
        cb_Monday.setEnabled(widgetFlag);
        cb_Tuesday.setEnabled(widgetFlag);
        cb_Wednesday.setEnabled(widgetFlag);
        cb_Thursday.setEnabled(widgetFlag);
        cb_Friday.setEnabled(widgetFlag);
        cb_Saturday.setEnabled(widgetFlag);
        cb_Sunday.setEnabled(widgetFlag);
        bn_from.setEnabled(widgetFlag);
        bn_to.setEnabled(widgetFlag);
        bn_alldays.setEnabled(widgetFlag);
        bn_nodays.setEnabled(widgetFlag);
        bn_workdays.setEnabled(widgetFlag);
        bn_we.setEnabled(widgetFlag);
        tx_validation.setEnabled(widgetFlag);
    }

    private void saveCalendarRule() {
        new SaveOperation().execute(ptRule);
    }

    private void changeCalendarRule() {
        ptRule = myNewRule;
        enableWidgets(false,true);
        validateActions();
    }



}
