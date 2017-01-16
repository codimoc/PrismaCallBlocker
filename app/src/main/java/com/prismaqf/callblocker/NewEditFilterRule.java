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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Class to create and edit a filter rule
 * Created by ConteDiMonteCristo.
 */
public class NewEditFilterRule extends NewEditActivity {


    private static final int EDIT_PATTERNS = 1001;

    private class DbOperation extends AsyncTask<FilterRule, Void, FilterRule> {

        private final String action;
        private final long ruleid;
        private boolean cantDelete = false;

        DbOperation(String action, long ruleid) {
            this.action = action;
            this.ruleid = ruleid;
        }
        @Override
        protected FilterRule doInBackground(FilterRule... rules) {
            SQLiteDatabase db = new DbHelper(NewEditFilterRule.this).getWritableDatabase();
            FilterRule rule = rules[0];
            try {
                switch (action) {
                    case NewEditActivity.ACTION_CREATE:
                        FilterRuleProvider.InsertRow(db, rule);
                        break;
                    case NewEditActivity.ACTION_EDIT:
                        FilterRuleProvider.UpdateFilterRule(db, ruleid, rule);
                        if (CallHelper.IsRunning() && FilterProvider.HasFilterRule(db,rule.getName()))
                            CallHelper.GetHelper().loadFilters(NewEditFilterRule.this);
                        break;
                    default:
                        if (FilterProvider.HasFilterRule(db,rule.getName())) {
                            cantDelete = true;
                        } else
                            FilterRuleProvider.DeleteFilterRule(db, ruleid);
                        break;
                }
            }
            finally {
                db.close();
                BackupManager.dataChanged(NewEditFilterRule.this.getPackageName());
            }
            return rule;
        }

        @Override
        protected void onPostExecute (FilterRule rule) {
            if (cantDelete) {
                Toast.makeText(getBaseContext(), getString(R.string.msg_can_not_delete_rule), Toast.LENGTH_LONG).show();
            }
            else if (myContext.equals(CONTEXT_PICK)) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(KEY_RULENAME,rule.getName());
                setResult(Activity.RESULT_OK,resultIntent);
                finish();
            }
            else if (myContext.equals(CONTEXT_EDIT)) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK,resultIntent);
                finish();
            }
            else if (myContext.equals(CONTEXT_SAVE)) {
                myContext = CONTEXT_EDIT;
                Intent intent = new Intent(NewEditFilterRule.this, NewEditFilterRule.class);
                intent.putExtra(NewEditActivity.KEY_NEW, (Parcelable)myNewRule);
                intent.putExtra(NewEditActivity.KEY_ORIG, (Parcelable)myOrigRule);
                intent.putExtra(NewEditActivity.KEY_ACTION, ACTION_EDIT);
                intent.putExtra(NewEditActivity.KEY_ISNAMEVALID, isNameValid);
                intent.putExtra(NewEditActivity.KEY_RULENAMES, myRuleNames);
                intent.putExtra(NewEditActivity.KEY_PTRULE, ptRule == myOrigRule ? "Original" : "New");
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(NewEditFilterRule.this, EditFilterRules.class);
                startActivity(intent);
            }
        }
    }
    private static final String TAG = NewEditFilterRule.class.getCanonicalName();

    private EditText ed_name, ed_description;
    private Button bn_managePatterns;
    private TextView tv_patterns, tv_validation;
    private ArrayList<String> myRuleNames;
    private String myAction, myContext;
    private FilterRule myNewRule, myOrigRule, ptRule;  //ptRule is an alias to the active rule
    private boolean isNameValid = true;
    private long myRuleId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_rule_edit);

        ed_name = (EditText) findViewById(R.id.edit_filter_rule_name);
        if (ed_name != null) ed_name.clearFocus();
        ed_description = (EditText) findViewById(R.id.edit_filter_rule_description);
        bn_managePatterns = (Button) findViewById(R.id.bt_filter_rule_patterns);
        tv_patterns = (TextView) findViewById(R.id.tx_rule_description);
        tv_validation = (TextView) findViewById(R.id.tx_filter_rule_validation);

        Intent intent = getIntent();
        myRuleNames = intent.getStringArrayListExtra(KEY_RULENAMES);
        //ACTION UPDATE
        if (intent.hasExtra(NewEditActivity.KEY_ACTION) &&
            intent.getStringExtra(NewEditActivity.KEY_ACTION).equals(NewEditActivity.ACTION_UPDATE)) {
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

        }
        //ACTION_EDIT
        else if (intent.hasExtra(NewEditActivity.KEY_ACTION) &&
                 intent.getStringExtra(NewEditActivity.KEY_ACTION).equals(NewEditActivity.ACTION_EDIT)) {
            myOrigRule = intent.getParcelableExtra(NewEditActivity.KEY_ORIG);
            myNewRule = intent.getParcelableExtra(NewEditActivity.KEY_NEW);
            myRuleId = intent.getLongExtra(NewEditActivity.KEY_RULEID,0);
            ptRule = myNewRule;
            myAction = NewEditActivity.ACTION_EDIT;

            enableWidgets(false,true);
        }
        //ACTION_CREATE
        else {
            myNewRule = new FilterRule("dummy","change me");
            myOrigRule = null;
            ptRule = myNewRule;
            myAction = NewEditActivity.ACTION_CREATE;

            enableWidgets(true,true);
        }
        tv_patterns.setText(makeRuleDescription());

        ed_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (ptRule != null)
                    ptRule.setDescription(ed_description.getText().toString());
                    validateActions();
            }
        });

        myContext = intent.hasExtra(KEY_CONTEXT)? intent.getStringExtra(KEY_CONTEXT): "none";

        if (getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private String makeRuleDescription() {
        StringBuilder builder = new StringBuilder("Patterns in rule. ");
        if (ptRule!=null) {
            builder.append(String.format(Locale.getDefault(),"Total of %d\n",ptRule.getPatternKeys().size()));
            int i=0;
            for (String p : ptRule.getPatternKeys()){
                builder.append(p);
                builder.append("  ");
                i++;
                if (i>10) {
                    builder.append("...");
                    break;
                }
            }
        }
        return builder.toString();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(NewEditActivity.KEY_NEW, myNewRule);
        savedInstanceState.putParcelable(NewEditActivity.KEY_ORIG, myOrigRule);
        savedInstanceState.putString(NewEditActivity.KEY_ACTION, myAction);
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
        myAction = savedInstanceState.getString(NewEditActivity.KEY_ACTION);
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
        myAction = NewEditActivity.ACTION_EDIT;
        ptRule = myNewRule;
        enableWidgets(false, true);
        validateActions();
    }

    @Override
    protected void undo() {
        myAction = NewEditActivity.ACTION_UPDATE;
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
                .setMessage(R.string.tx_rule_delete_confirm)
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
        alert.setTitle(R.string.tx_filter_rule_help_title);

        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_asset/html/filter_rule_edit.html");
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
        return ptRule!=null && !ptRule.equals(myOrigRule);
    }

    @Override
    protected RuleNameValidator getRuleNameValidator() {
        return new RuleNameValidator(ed_name, tv_validation, myRuleNames) {
            @Override
            public void validate(TextView source, TextView target, ArrayList<String> names, String text) {
                ptRule.setName(text);
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
        if (mi_save==null || mi_delete ==null || mi_change == null || mi_undo == null) return;
        //Save only valie in EDIT or CREATE mode, when the data has change and the name is valid
        mi_save.setVisible((myAction.equals(NewEditActivity.ACTION_EDIT) ||
                myAction.equals(NewEditActivity.ACTION_CREATE)) &&
                !myNewRule.equals(myOrigRule) && isNameValid);
        //Delete only valid in UPDATE mode
        mi_delete.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptRule == myOrigRule);
        //Change only valid in UPDATE mode
        mi_change.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptRule == myOrigRule);
        mi_change.setVisible(myAction.equals(NewEditActivity.ACTION_UPDATE) && ptRule == myOrigRule);
        //Undo only valid in EDIT mode where there have been changes
        mi_undo.setVisible(myAction.equals(NewEditActivity.ACTION_EDIT) &&
                ptRule == myNewRule &&
                !myNewRule.equals(myOrigRule) &&
                isNameValid);
        if (myAction.equals(NewEditActivity.ACTION_EDIT)) {
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
        bn_managePatterns.setEnabled(widgetFlag);
        tv_patterns.setEnabled(widgetFlag);
        tv_validation.setEnabled(widgetFlag);
    }

    @Override
    void refreshWidgets(boolean validate) {
        ed_name.setText(ptRule.getName());
        ed_description.setText(ptRule.getDescription());
        tv_patterns.setText(makeRuleDescription());
        super.refreshWidgets(validate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_PATTERNS) {
            FilterRule updatedRule = data.getParcelableExtra(KEY_PTRULE);
            ptRule.clearPatterns();
            for(String pattern: updatedRule.getPatternKeys())
                ptRule.addPattern(pattern);
            refreshWidgets(true);
            if (!myAction.equals(ACTION_CREATE) && mi_save != null && mi_save.isVisible()) {
                myContext = CONTEXT_SAVE;
                save();
            }

        }
    }

    public void onManagePatterns(View view) {
        Intent intent = new Intent(this, EditFilterPatterns.class);
        intent.putExtra(KEY_PTRULE,(Parcelable)ptRule);
        intent.putExtra(KEY_ORIG,(Parcelable)ptRule);
        startActivityForResult(intent,EDIT_PATTERNS);
    }
}
