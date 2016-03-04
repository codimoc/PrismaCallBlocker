package com.prismaqf.callblocker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Activity to create or edit a calendar rule
 * @author ConteDiMonteCristo
 */
public class NewEditCalendarRule extends NewEditActivity {


    @SuppressLint("ValidFragment")
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        final CalendarRule rule;
        final String startEnd;
        final NewEditCalendarRule parent;


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

    private class DbOperation extends AsyncTask<CalendarRule, Void, Void> {

        private final String action;
        private final long ruleid;

        DbOperation(String action, long ruleid) {
            this.action = action;
            this.ruleid = ruleid;
        }
        @Override
        protected Void doInBackground(CalendarRule... rules) {
            SQLiteDatabase db = new DbHelper(NewEditCalendarRule.this).getWritableDatabase();
            CalendarRule rule = rules[0];
            try {
                if (action.equals(NewEditActivity.ACTION_CREATE))
                    CalendarRuleProvider.InsertRow(db, rule);
                else if (action.equals(NewEditActivity.ACTION_UPDATE))
                    CalendarRuleProvider.UpdateCalendarRule(db, ruleid, rule);
                else //ACTION_DELETE
                    CalendarRuleProvider.DeleteCalendarRule(db, ruleid);
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
    private CheckBox cb_Monday, cb_Tuesday, cb_Wednesday, cb_Thursday, cb_Friday, cb_Saturday, cb_Sunday;
    private EditText ed_name;
    private TextView tx_validation;
    private Button bn_from, bn_to, bn_alldays, bn_nodays, bn_workdays, bn_we;
    private CalendarRule myNewRule, myOrigRule, ptRule;  //ptRule is an alias to the active rule
    private ArrayList<String> myRuleNames;
    private String myAction;
    private long myRuleId=0;
    private boolean isNameValid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_rule_edit);

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
        myRuleNames = intent.getStringArrayListExtra(NewEditActivity.KEY_RULENAMES);


        if (intent.hasExtra(NewEditActivity.ACTION_KEY) && intent.getStringExtra(NewEditActivity.ACTION_KEY).equals(NewEditActivity.ACTION_UPDATE)) {
            myOrigRule = intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            try {
                myNewRule  = (CalendarRule)myOrigRule.clone();
            } catch (CloneNotSupportedException e) {
                Log.e(TAG, "Could not clone original rule");
                myNewRule =  intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            }
            myRuleId = intent.getLongExtra(NewEditActivity.KEY_RULEID,0);
            ptRule = myOrigRule;
            myAction = NewEditActivity.ACTION_UPDATE;

            enableWidgets(false,false);

        } else {
            myNewRule = new CalendarRule(); //always active by default (all days of week and full day)
            myOrigRule = null;
            ptRule = myNewRule;
            myAction = NewEditActivity.ACTION_CREATE;

            enableWidgets(true,true);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(NewEditActivity.KEY_NEW, myNewRule);
        savedInstanceState.putParcelable(NewEditActivity.KEY_ORIG, myOrigRule);
        savedInstanceState.putString(NewEditActivity.ACTION_KEY, myAction);
        savedInstanceState.putBoolean(NewEditActivity.KEY_ISNAMEVALID, isNameValid);
        savedInstanceState.putStringArrayList(NewEditActivity.KEY_RULENAMES, myRuleNames);
        savedInstanceState.putString(NewEditActivity.KEY_PTRULE, ptRule == myOrigRule ? "Original" : "New");

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myNewRule = savedInstanceState.getParcelable(NewEditActivity.KEY_NEW);
        myOrigRule = savedInstanceState.getParcelable(NewEditActivity.KEY_ORIG);
        myAction = savedInstanceState.getString(NewEditActivity.ACTION_KEY);
        isNameValid = savedInstanceState.getBoolean(NewEditActivity.KEY_ISNAMEVALID);
        myRuleNames = savedInstanceState.getStringArrayList(NewEditActivity.KEY_RULENAMES);
        String rule = savedInstanceState.getString(NewEditActivity.KEY_PTRULE,"");
        if (rule.equals("Original"))
            ptRule = myOrigRule;
        else
            ptRule = myNewRule;
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


    @Override
    protected void refreshWidgets(boolean validate) {
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
        super.refreshWidgets(validate);
    }

    @Override
    protected RuleNameValidator getRuleNameValidator() {
        return new RuleNameValidator(ed_name, tx_validation, myRuleNames) {
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
        };
    }

    @Override
    protected EditText getNameEditField() {
        return ed_name;
    }

    @Override
    protected String getAction() {
        return myAction;
    }

    @Override
    protected void validateActions() {
        mi_save.setVisible(!myNewRule.equals(myOrigRule) && isNameValid);
        mi_delete.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptRule == myOrigRule);
        mi_change.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptRule == myOrigRule);
        mi_undo.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) &&
                ptRule == myNewRule &&
                !myNewRule.equals(myOrigRule) &&
                isNameValid);
        if (myAction.equals(NewEditActivity.ACTION_UPDATE)) {
            if (myNewRule.equals(myOrigRule))
                tx_validation.setText(R.string.tx_validation_rule_no_changes);
            else
                tx_validation.setText(R.string.tx_validation_rule_has_changed);
        }

    }

    @Override
    protected void enableWidgets(boolean nameFlag, boolean widgetFlag) {
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

    @Override
    protected void save() {
        new DbOperation(myAction,myRuleId).execute(ptRule);
    }

    @Override
    protected void change() {
        ptRule = myNewRule;
        enableWidgets(false,true);
        validateActions();
    }

    @Override
    protected void undo() {
        ptRule = myOrigRule;
        try {
            myNewRule  = (CalendarRule)myOrigRule.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "Could not clone original rule");
        }
        refreshWidgets(true);
        enableWidgets(false, false);
    }

    @Override
    protected void delete() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.tx_calendar_rule_delete_confirm)
                .setCancelable(false)
                .setPositiveButton(R.string.bt_yes_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DbOperation(NewEditActivity.ACTION_DELETE, myRuleId).execute(ptRule);
                    }
                })
                .setNegativeButton(R.string.bt_no_keep,null)
                .show();
    }

    @Override
    protected void help() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.tx_calendar_rule_help_title);

        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_asset/html/calendar_rule_edit.html");
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                          ViewGroup.LayoutParams.MATCH_PARENT));
        scroll.addView(wv);

        alert.setView(scroll);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }



}
