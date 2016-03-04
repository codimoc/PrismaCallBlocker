package com.prismaqf.callblocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to create and edit a filter rule
 * Created by ConteDiMonteCristo.
 */
public class NewEditFilterRule extends NewEditActivity {

    private class DbOperation extends AsyncTask<FilterRule, Void, Void> {

        private final String action;
        private final long ruleid;

        DbOperation(String action, long ruleid) {
            this.action = action;
            this.ruleid = ruleid;
        }
        @Override
        protected Void doInBackground(FilterRule... rules) {
            SQLiteDatabase db = new DbHelper(NewEditFilterRule.this).getWritableDatabase();
            FilterRule rule = rules[0];
            try {
                switch (action) {
                    case NewEditActivity.ACTION_CREATE:
                        FilterRuleProvider.InsertRow(db, rule);
                        break;
                    case NewEditActivity.ACTION_UPDATE:
                        FilterRuleProvider.UpdateFilterRule(db, ruleid, rule);
                        break;
                    default:
                        FilterRuleProvider.DeleteFilterRule(db, ruleid);
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
            Intent intent = new Intent(NewEditFilterRule.this, EditFilterRules.class);
            startActivity(intent);
        }
    }
    private static final String TAG = NewEditFilterRule.class.getCanonicalName();

    private EditText ed_name, ed_description, ed_pattern;
    private Button bn_pickLog, bn_delSelected, bn_insPattern;
    private ListView lv_patterns;
    private TextView tv_validation;
    private ArrayList<String> myRuleNames, myPatterns;
    private String myAction;
    private FilterRule myNewRule, myOrigRule, ptRule;  //ptRule is an alias to the active rule
    private boolean isNameValid = true;
    private long myRuleId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_rule_edit);

        ed_name = (EditText) findViewById(R.id.edit_filter_rule_name);
        ed_name.clearFocus();
        ed_description = (EditText) findViewById(R.id.edit_filter_rule_description);
        bn_pickLog = (Button) findViewById(R.id.bt_pick_log);
        bn_delSelected = (Button) findViewById(R.id.bt_delete_selected);
        bn_insPattern = (Button) findViewById(R.id.bt_insert_pattern);
        ed_pattern = (EditText) findViewById(R.id.edit_insert_pattern);
        lv_patterns = (ListView) findViewById(R.id.list_view_patterns);
        tv_validation = (TextView) findViewById(R.id.tx_filter_rule_validation);

        Intent intent = getIntent();
        myRuleNames = intent.getStringArrayListExtra(KEY_RULENAMES);

        if (intent.hasExtra(NewEditActivity.ACTION_KEY) &&
            intent.getStringExtra(NewEditActivity.ACTION_KEY).equals(NewEditActivity.ACTION_UPDATE)) {
            myOrigRule = intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            try {
                myNewRule  = (FilterRule)myOrigRule.clone();
            } catch (CloneNotSupportedException e) {
                Log.e(TAG, "Could not clone original rule");
                myNewRule =  intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            }
            myRuleId = intent.getLongExtra(NewEditActivity.KEY_RULEID,0);
            ptRule = myOrigRule;
            myAction = NewEditActivity.ACTION_UPDATE;

            enableWidgets(false,false);

        } else {
            myNewRule = new FilterRule("dummy","change me");
            myOrigRule = null;
            ptRule = myNewRule;
            myAction = NewEditActivity.ACTION_CREATE;

            enableWidgets(true,true);
        }
        myPatterns = new ArrayList<>();
        myPatterns.addAll(ptRule.getPatternKeys());
        lv_patterns.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,myPatterns));

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
            myNewRule  = (FilterRule)myOrigRule.clone();
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
        //todo: implement this
    }

    @Override
    protected RuleNameValidator getRuleNameValidator() {
        return new RuleNameValidator(ed_name, tv_validation, myRuleNames) {
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
                tv_validation.setText(R.string.tx_validation_rule_no_changes);
            else
                tv_validation.setText(R.string.tx_validation_rule_has_changed);
        }
    }

    @Override
    protected void enableWidgets(boolean nameFlag, boolean widgetFlag) {
        ed_name.setEnabled(nameFlag);
        ed_description.setEnabled(widgetFlag);
        bn_pickLog.setEnabled(widgetFlag);
        bn_delSelected.setEnabled(widgetFlag);
        bn_insPattern.setEnabled(widgetFlag);
        ed_pattern.setEnabled(widgetFlag);
        lv_patterns.setEnabled(widgetFlag);
        tv_validation.setEnabled(widgetFlag);
    }

    @Override
    protected void refreshWidgets(boolean validate) {
        ed_name.setText(ptRule.getName());
        ed_description.setText(ptRule.getName());
        ed_pattern.setText("");
        myPatterns.clear();
        myPatterns.addAll(ptRule.getPatternKeys());
        lv_patterns.getAdapter().notify();
        super.refreshWidgets(validate);
    }
}
