package com.prismaqf.callblocker;

import android.os.Bundle;
import android.widget.EditText;

/**
 * Class to create and edit a filter rule
 * Created by ConteDiMonteCristo.
 */
public class NewEditFilterRule extends NewEditActivity {
    private static final String TAG = NewEditFilterRule.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_rule_edit);
        //todo: implement
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //todo: implement
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //todo: implement
    }

    @Override
    protected void save() {

    }

    @Override
    protected void change() {

    }

    @Override
    protected void undo() {

    }

    @Override
    protected void delete() {

    }

    @Override
    protected void help() {

    }

    @Override
    protected RuleNameValidator getRuleNameValidator() {
        return null;
    }

    @Override
    protected EditText getNameEditField() {
        return null;
    }

    @Override
    protected String getAction() {
        return null;
    }

    @Override
    protected void validateActions() {

    }

    @Override
    protected void enableWidgets(boolean nameFlag, boolean widgetFlag) {

    }

    @Override
    protected void refreshWidgets(boolean validate) {
        //todo: implement
        super.refreshWidgets(validate);
    }
}
