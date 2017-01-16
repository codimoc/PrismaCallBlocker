package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.util.ArrayList;

/**
 * Class to create and edit a filter
 * Created by ConteDiMonteCristo.
 */
public class NewEditFilter extends NewEditActivity{

    private class DbOperation extends AsyncTask<FilterHandle, Void, Void> {

        private final String action;
        private final long filterid;

        DbOperation(String action, long filterid) {
            this.action = action;
            this.filterid = filterid;
        }
        @Override
        protected Void doInBackground(FilterHandle... filters) {
            SQLiteDatabase db = new DbHelper(NewEditFilter.this).getWritableDatabase();
            FilterHandle filter = filters[0];
            try {
                switch (action) {
                    case NewEditActivity.ACTION_CREATE:
                        FilterProvider.InsertRow(db, filter);
                        break;
                    case NewEditActivity.ACTION_EDIT:
                        FilterProvider.UpdateFilter(db, filterid, filter);
                        break;
                    default:
                        FilterProvider.DeleteFilter(db, filterid);
                        break;
                }
                if (CallHelper.IsRunning())
                    CallHelper.GetHelper().loadFilters(NewEditFilter.this);
            }
            finally {
                db.close();
                BackupManager.dataChanged(NewEditFilter.this.getPackageName());
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void v) {
            Intent intent = new Intent(NewEditFilter.this, EditFilters.class);
            startActivity(intent);
        }
    }

    private class DbCalendar extends AsyncTask<String, Void, CalendarRule> {

        private String myRuleName;
        private long myRuleId = 0;

        @Override
        protected CalendarRule doInBackground(String... names) {
            SQLiteDatabase db = new DbHelper(NewEditFilter.this).getReadableDatabase();
            myRuleName = names[0];
            try {
                myRuleId = CalendarRuleProvider.FindCalendarRuleId(db,myRuleName);
                return CalendarRuleProvider.FindCalendarRule(db, myRuleName);

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            finally {
                db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute (CalendarRule rule) {
            Intent intent = new Intent(NewEditFilter.this,NewEditCalendarRule.class);
            intent.putExtra(NewEditActivity.KEY_ACTION, NewEditActivity.ACTION_UPDATE);
            intent.putExtra(NewEditActivity.KEY_CONTEXT, NewEditActivity.CONTEXT_EDIT);
            intent.putExtra(NewEditActivity.KEY_ORIG,(Parcelable)rule);
            intent.putExtra(NewEditActivity.KEY_RULEID,myRuleId);
            startActivityForResult(intent, EDIT_CAL);
        }
    }

    private class DbPatterns extends AsyncTask<String, Void, FilterRule> {

        private String myRuleName;
        private long myRuleId = 0;

        @Override
        protected FilterRule doInBackground(String... names) {
            SQLiteDatabase db = new DbHelper(NewEditFilter.this).getReadableDatabase();
            myRuleName = names[0];
            try {
                myRuleId = FilterRuleProvider.FindFilterRuleId(db, myRuleName);
                return FilterRuleProvider.FindFilterRule(db, myRuleName);

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            finally {
                db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute (FilterRule rule) {
            Intent intent = new Intent(NewEditFilter.this,NewEditFilterRule.class);
            intent.putExtra(NewEditActivity.KEY_ACTION, NewEditActivity.ACTION_UPDATE);
            intent.putExtra(NewEditActivity.KEY_CONTEXT, NewEditActivity.CONTEXT_EDIT);
            intent.putExtra(NewEditActivity.KEY_ORIG,(Parcelable)rule);
            intent.putExtra(NewEditActivity.KEY_RULEID,myRuleId);
            startActivityForResult(intent, EDIT_PAT);
        }
    }

    private static final String TAG = NewEditFilter.class.getCanonicalName();
    private static final int PICK_CAL = 1001;
    private static final int PICK_PAT = 1002;
    private static final int PICK_ACT = 1003;
    private static final int EDIT_CAL = 1004;
    private static final int EDIT_PAT = 1005;
    private EditText ed_name;
    private TextView tv_calendar_name, tv_paterns_name, tv_action_name, tv_validation;
    private MenuItem mi_pickCalendar, mi_pickPatterns, mi_pickAction, mi_editCalendar, mi_editPatterns;
    private ArrayList<String> filterNames;
    private String myAction;
    private FilterHandle myNewFilter, myOrigFilter, ptFilter;  //ptFilter is an alias to the active filter
    private boolean isNameValid = true;
    private long myFilterId=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_edit);

        ed_name = (EditText) findViewById(R.id.edit_filter_name);
        if (ed_name != null) ed_name.clearFocus();
        tv_calendar_name = (TextView) findViewById(R.id.text_calendar_name);
        tv_paterns_name = (TextView) findViewById(R.id.text_filter_rule_name);
        tv_action_name = (TextView) findViewById(R.id.text_action_name);
        tv_validation = (TextView) findViewById(R.id.tx_filter_rule_validation);

        Intent intent = getIntent();
        filterNames = intent.getStringArrayListExtra(KEY_FILTERNAMES);
        //ACTION UPDATE
        if (intent.hasExtra(NewEditActivity.KEY_ACTION) &&
                intent.getStringExtra(NewEditActivity.KEY_ACTION).equals(NewEditActivity.ACTION_UPDATE)) {
            myOrigFilter = intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            try {
                myNewFilter  = (FilterHandle)myOrigFilter.clone();
            } catch (CloneNotSupportedException e) {
                Log.e(TAG, "Could not clone original filter");
                myNewFilter =  intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            }
            myFilterId = intent.getLongExtra(NewEditActivity.KEY_RULEID,0);
            ptFilter = myOrigFilter;
            myAction = NewEditActivity.ACTION_UPDATE;

            enableWidgets(false,false);

        }
        //ACTION_EDIT
        else if (intent.hasExtra(NewEditActivity.KEY_ACTION) &&
                intent.getStringExtra(NewEditActivity.KEY_ACTION).equals(NewEditActivity.ACTION_EDIT)) {
            myOrigFilter = intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            myNewFilter = intent.getParcelableExtra(NewEditActivity.KEY_NEW);
            myFilterId = intent.getLongExtra(NewEditActivity.KEY_RULEID,0);
            ptFilter = myNewFilter;
            myAction = NewEditActivity.ACTION_EDIT;

            enableWidgets(false,true);
        }
        //ACTION_CREATE
        else {
            myNewFilter = new FilterHandle("dummy",null,null,null);
            myOrigFilter = null;
            ptFilter = myNewFilter;
            myAction = NewEditActivity.ACTION_CREATE;

            enableWidgets(true,true);
        }

        if (getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean flag = super.onCreateOptionsMenu(menu);
        mi_pickCalendar = menu.findItem(R.id.action_pick_calendar);
        mi_pickPatterns = menu.findItem(R.id.action_pick_patterns);
        mi_pickAction = menu.findItem(R.id.action_pick_action);
        mi_editCalendar = menu.findItem(R.id.action_edit_calendar);
        mi_editPatterns = menu.findItem(R.id.action_edit_patterns);
        if (myAction.equals(ACTION_CREATE)) {
            mi_pickCalendar.setVisible(true);
            mi_pickPatterns.setVisible(true);
            mi_pickAction.setVisible(true);
        }
        else if (myAction.equals(ACTION_EDIT)) {
            mi_pickCalendar.setVisible(true);
            mi_pickPatterns.setVisible(true);
            mi_pickAction.setVisible(true);
            mi_editCalendar.setVisible(true);
            mi_editPatterns.setVisible(true);
        }

        return flag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_pick_calendar:
                pickCalendar();
                return true;
            case R.id.action_pick_patterns:
                pickPatterns();
                return true;
            case R.id.action_pick_action:
                pickAction();
                return true;
            case R.id.action_edit_calendar:
                editCalendar();
                return true;
            case R.id.action_edit_patterns:
                editPatterns();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(NewEditActivity.KEY_NEW, myNewFilter);
        savedInstanceState.putParcelable(NewEditActivity.KEY_ORIG, myOrigFilter);
        savedInstanceState.putString(NewEditActivity.KEY_ACTION, myAction);
        savedInstanceState.putBoolean(NewEditActivity.KEY_ISNAMEVALID, isNameValid);
        savedInstanceState.putStringArrayList(NewEditActivity.KEY_RULENAMES, filterNames);
        savedInstanceState.putString(NewEditActivity.KEY_PTRULE, ptFilter == myOrigFilter ? "Original" : "New");

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myNewFilter = savedInstanceState.getParcelable(NewEditActivity.KEY_NEW);
        myOrigFilter = savedInstanceState.getParcelable(NewEditActivity.KEY_ORIG);
        myAction = savedInstanceState.getString(NewEditActivity.KEY_ACTION);
        isNameValid = savedInstanceState.getBoolean(NewEditActivity.KEY_ISNAMEVALID);
        filterNames = savedInstanceState.getStringArrayList(NewEditActivity.KEY_RULENAMES);
        String rule = savedInstanceState.getString(NewEditActivity.KEY_PTRULE,"");
        if (rule.equals("Original"))
            ptFilter = myOrigFilter;
        else
            ptFilter = myNewFilter;
    }

    @Override
    protected void save() {
        new DbOperation(myAction,myFilterId).execute(ptFilter);
    }

    @Override
    protected void change() {
        myAction = NewEditActivity.ACTION_EDIT;
        ptFilter = myNewFilter;
        enableWidgets(false,true);
        validateActions();
    }

    @Override
    protected void undo() {
        myAction = NewEditActivity.ACTION_UPDATE;
        ptFilter = myOrigFilter;
        try {
            myNewFilter  = (FilterHandle)myOrigFilter.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "Could not clone original rule");
        }
        refreshWidgets(true);
        enableWidgets(false, false);
    }

    @Override
    protected void delete() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.tx_filter_delete_confirm)
                .setCancelable(false)
                .setPositiveButton(R.string.bt_yes_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DbOperation(NewEditActivity.ACTION_DELETE, myFilterId).execute(ptFilter);
                    }
                })
                .setNegativeButton(R.string.bt_no_keep,null)
                .show();
    }

    @Override
    protected void help() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.tx_filter_help_title);

        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_asset/html/filter_edit.html");
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

    @Override
    protected boolean hasChanged() {
        return ptFilter!=null && !ptFilter.equals(myOrigFilter);
    }

    @Override
    protected void validateActions() {
        if (mi_save==null || mi_delete ==null || mi_change == null || mi_undo == null) return;
        //Save only valie in EDIT or CREATE mode, when the data has change and the name is valid
        mi_save.setVisible((myAction.equals(NewEditActivity.ACTION_EDIT) ||
                myAction.equals(NewEditActivity.ACTION_CREATE)) &&
                !myNewFilter.equals(myOrigFilter) && isNameValid);
        //Delete only valid in UPDATE mode
        mi_delete.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptFilter == myOrigFilter);
        //Change only valid in UPDATE mode
        mi_change.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptFilter == myOrigFilter);
        mi_change.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptFilter == myOrigFilter);
        //Undo only valid in EDIT mode where there have been changes
        mi_undo.setVisible(myAction.equals(NewEditActivity.ACTION_EDIT) &&
                ptFilter == myNewFilter &&
                !myNewFilter.equals(myOrigFilter) &&
                isNameValid);
        if (myAction.equals(NewEditActivity.ACTION_EDIT)) {
            if (myNewFilter.equals(myOrigFilter))
                tv_validation.setText(R.string.tx_validation_rule_no_changes);
            else
                tv_validation.setText(R.string.tx_validation_rule_has_changed);
        }
        boolean canChangeFilter = ((myAction.equals(NewEditActivity.ACTION_EDIT) || myAction.equals(NewEditActivity.ACTION_CREATE)) &&
                                   ptFilter == myNewFilter);
        if (mi_pickCalendar != null) mi_pickCalendar.setVisible(canChangeFilter);
        if (mi_pickPatterns != null) mi_pickPatterns.setVisible(canChangeFilter);
        if (mi_pickAction != null) mi_pickAction.setVisible(canChangeFilter);
        if (mi_editCalendar != null) mi_editCalendar.setVisible(canChangeFilter);
        if (mi_editPatterns != null) mi_editPatterns.setVisible(canChangeFilter);
    }

    @Override
    protected RuleNameValidator getRuleNameValidator() {
        return new RuleNameValidator(ed_name, tv_validation, filterNames) {
            @Override
            public void validate(TextView source, TextView target, ArrayList<String> names, String text) {
                ptFilter.setName(text);
                if (source.getText().toString().equals("")) {
                    target.setText(R.string.tx_validation_filter_name_empty);
                    mi_save.setVisible(false);
                    isNameValid = false;
                    return;
                }
                if (names!= null && names.contains(source.getText().toString())) {
                    target.setText(R.string.tx_validation_filter_name_used);
                    mi_save.setVisible(false);
                    isNameValid = false;
                    return;
                }
                mi_save.setVisible(true);
                target.setText(R.string.tx_validation_filter_valid);
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
    protected void enableWidgets(boolean nameFlag, boolean widgetFlag) {
        ed_name.setEnabled(nameFlag);
        tv_calendar_name.setEnabled(widgetFlag);
        tv_paterns_name.setEnabled(widgetFlag);
        tv_action_name.setEnabled(widgetFlag);
        tv_validation.setEnabled(widgetFlag);
    }

    @Override
    void refreshWidgets(boolean validate) {
        ed_name.setText(ptFilter.getName());
        tv_calendar_name.setText(String.format("%s %s", getString(R.string.tx_calendar_rule_name), ptFilter.getCalendarRuleName()));
        tv_paterns_name.setText(String.format("%s %s", getString(R.string.tx_filter_rule_name), ptFilter.getFilterRuleName()));
        tv_action_name.setText(String.format("%s %s", getString(R.string.tx_action_name), ptFilter.getActionSimpleName()));
        super.refreshWidgets(validate);
    }

    private void pickCalendar() {
        Intent intent = new Intent(this,EditCalendarRules.class);
        intent.putExtra(NewEditActivity.KEY_CONTEXT, NewEditActivity.CONTEXT_PICK);
        startActivityForResult(intent, PICK_CAL);
    }

    private void pickPatterns() {
        Intent intent = new Intent(this,EditFilterRules.class);
        intent.putExtra(NewEditActivity.KEY_CONTEXT, NewEditActivity.CONTEXT_PICK);
        startActivityForResult(intent, PICK_PAT);
    }

    private void pickAction() {
        Intent intent = new Intent(this,PickAction.class);
        startActivityForResult(intent, PICK_ACT);
    }

    private void editCalendar() {
        new DbCalendar().execute(ptFilter.getCalendarRuleName());
    }

    private void editPatterns() {
        new DbPatterns().execute(ptFilter.getFilterRuleName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_CAL) {
            ptFilter.setCalendarRuleName(data.getStringExtra(KEY_RULENAME));
            refreshWidgets(true);
            return;
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_PAT) {
            ptFilter.setFilterRuleName(data.getStringExtra(KEY_RULENAME));
            refreshWidgets(true);
            return;
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_ACT) {
            ptFilter.setActionName(data.getStringExtra(KEY_ACTIONNAME));
            refreshWidgets(true);
            return;
        }
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_CAL) {
            refreshWidgets(true);
            return;
        }
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_PAT) {
            refreshWidgets(true);
        }
    }

}
