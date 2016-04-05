package com.prismaqf.callblocker;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Abstract base class for rule editing
 * @author ConteDiMonteCristo
 */
public abstract class NewEditActivity extends AppCompatActivity {

    static final String KEY_NEW  = "com.prismaqft.callblocker:keynew";
    static final String KEY_ORIG  = "com.prismaqft.callblocker:keyorig";
    static final String KEY_ISNAMEVALID  = "com.prismaqft.callblocker:namevalid";
    static final String KEY_RULENAMES  = "com.prismaqft.callblocker:rulenames";
    static final String KEY_FILTERNAMES  = "com.prismaqft.callblocker:rulenames";
    static final String KEY_PTRULE  = "com.prismaqft.callblocker:ptrule";
    static final String KEY_RULENAME = "com.prismaqft.callblocker:rulename";
    static final String KEY_RULEID = "com.prismaqft.callblocker:ruleid";
    static final String ACTION_CREATE  = "com.prismaqf.callblocker:create";
    static final String ACTION_UPDATE  = "com.prismaqf.callblocker:update";
    static final String ACTION_EDIT  = "com.prismaqf.callblocker:edit";
    static final String ACTION_DELETE  = "com.prismaqf.callblocker:delete";
    static final String CONTEXT_PICK = "com.prismaqf.callblocker:pick";
    static final String KEY_ACTION = "com.prismaqf.callblocker:action";
    static final String KEY_CONTEXT = "com.prismaqf.callblocker:context";
    public static final String KEY_NUMBER = "com.prismaqf.callblocker:number";
    public static final String KEY_CHECKED = "com.prismaqf.callblocker:checked";

    MenuItem mi_save;
    MenuItem mi_delete;
    MenuItem mi_change;
    MenuItem mi_undo;

    protected abstract void save();
    protected abstract void change();
    protected abstract void undo();
    protected abstract void delete();
    protected abstract void help();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //add text validation
        getNameEditField().addTextChangedListener(getRuleNameValidator());

        if (getSupportActionBar()!= null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_rules, menu);
        mi_save = menu.findItem(R.id.action_save);
        mi_delete = menu.findItem(R.id.action_delete);
        mi_change = menu.findItem(R.id.action_change);
        mi_undo = menu.findItem(R.id.action_undo);
        if (getAction().equals(NewEditActivity.ACTION_CREATE)) {
            mi_delete.setVisible(false);
            mi_change.setVisible(false);
            mi_undo.setVisible(false);
            enableWidgets(true,true);
        }
        else if (getAction().equals(NewEditActivity.ACTION_EDIT)) {
            mi_change.setVisible(false);
            mi_delete.setVisible(false);
            enableWidgets(false, true);
        }
        else if (getAction().equals(NewEditActivity.ACTION_UPDATE)) {
            mi_save.setVisible(false);
            mi_undo.setVisible(false);
            enableWidgets(false,false);
        }
        refreshWidgets(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
                save();
                return true;
            case R.id.action_change:
                change();
                return true;
            case R.id.action_undo:
                undo();
                return true;
            case R.id.action_delete:
                delete();
                return true;
            case R.id.action_help:
                help();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void refreshWidgets(boolean validate) {
        if (validate) validateActions();
    }

    protected abstract void validateActions();

    protected abstract RuleNameValidator getRuleNameValidator();
    protected abstract EditText getNameEditField();
    protected abstract String getAction();
    protected abstract void enableWidgets(boolean nameFlag, boolean widgetFlag);
}
