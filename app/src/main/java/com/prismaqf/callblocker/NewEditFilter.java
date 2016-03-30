package com.prismaqf.callblocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;

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
            }
            finally {
                db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void v) {
            Intent intent = new Intent(NewEditFilter.this, EditFilters.class);
            startActivity(intent);
        }
    }

    private static final String TAG = NewEditFilter.class.getCanonicalName();
    private EditText ed_name;
    private TextView tv_calendar_name, tv_calendar_desc, tv_paterns_name, tv_patterns_desc,
                     tv_action_name, tv_action_desc, tv_validation;
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
        ed_name.clearFocus();
        tv_calendar_name = (TextView) findViewById(R.id.text_calendar_name);
        tv_calendar_desc = (TextView) findViewById(R.id.text_calendar_format);
        tv_paterns_name = (TextView) findViewById(R.id.text_filter_rule_name);
        tv_patterns_desc = (TextView) findViewById(R.id.text_filter_rule_description);
        tv_action_name = (TextView) findViewById(R.id.text_action_name);
        tv_action_desc = (TextView) findViewById(R.id.text_action_description);
        tv_validation = (TextView) findViewById(R.id.tx_filter_rule_validation);

        Intent intent = getIntent();
        filterNames = intent.getStringArrayListExtra(KEY_FILTERNAMES);
        //ACTION UPDATE
        if (intent.hasExtra(NewEditActivity.ACTION_KEY) &&
                intent.getStringExtra(NewEditActivity.ACTION_KEY).equals(NewEditActivity.ACTION_UPDATE)) {
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
        else if (intent.hasExtra(NewEditActivity.ACTION_KEY) &&
                intent.getStringExtra(NewEditActivity.ACTION_KEY).equals(NewEditActivity.ACTION_EDIT)) {
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(NewEditActivity.KEY_NEW, myNewFilter);
        savedInstanceState.putParcelable(NewEditActivity.KEY_ORIG, myOrigFilter);
        savedInstanceState.putString(NewEditActivity.ACTION_KEY, myAction);
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
        myAction = savedInstanceState.getString(NewEditActivity.ACTION_KEY);
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
        //todo: implement this
    }

    @Override
    protected void validateActions() {
        //todo: implement this
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
        tv_validation.setEnabled(widgetFlag);
    }

    @Override
    protected void refreshWidgets(boolean validate) {
        ed_name.setText(ptFilter.getName());
        super.refreshWidgets(validate);
    }
}
