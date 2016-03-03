package com.prismaqf.callblocker;

import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Abstract base class for rule editing
 * @author ConteDiMonteCristo
 */
public abstract class NewEditActivity extends ActionBarActivity {

    protected static final String ACTION_KEY  = "com.prismaqft.callblocker:key";
    protected static final String KEY_NEW  = "com.prismaqft.callblocker:keynew";
    protected static final String KEY_ORIG  = "com.prismaqft.callblocker:keyorig";
    protected static final String KEY_ISNAMEVALID  = "com.prismaqft.callblocker:namevalid";
    protected static final String KEY_RULENAMES  = "com.prismaqft.callblocker:rulenames";
    protected static final String KEY_PTRULE  = "com.prismaqft.callblocker:ptrule";
    protected static final String KEY_RULEID = "com.prismaqft.callblocker:ruleid";
    protected static final String ACTION_CREATE  = "com.prismaqf.callblocker:create";
    protected static final String ACTION_UPDATE  = "com.prismaqf.callblocker:update";
    protected static final String ACTION_DELETE  = "com.prismaqf.callblocker:delete";

    protected MenuItem mi_save, mi_delete, mi_change, mi_undo;

    protected abstract void save();
    protected abstract void change();
    protected abstract void undo();
    protected abstract void delete();
    protected abstract void help();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_rules, menu);
        mi_save = menu.findItem(R.id.action_save_rule);
        mi_delete = menu.findItem(R.id.action_delete_rule);
        mi_change = menu.findItem(R.id.action_change_rule);
        mi_undo = menu.findItem(R.id.action_undo_rule);
        mi_undo.setVisible(false);
        if (getAction().equals(NewEditActivity.ACTION_CREATE)) {
            mi_delete.setVisible(false);
            mi_change.setVisible(false);
        }
        else {
            mi_save.setVisible(false);
        }

        //add text validation
        getNameEditField().addTextChangedListener(getRuleNameValidator());
        refreshWidgets(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save_rule:
                save();
                return true;
            case R.id.action_change_rule:
                change();
                return true;
            case R.id.action_undo_rule:
                undo();
                return true;
            case R.id.action_delete_rule:
                delete();
                return true;
            case R.id.action_help_rule:
                help();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void refreshWidgets(boolean validate) {
        if (validate) validateActions();
    }

    protected abstract RuleNameValidator getRuleNameValidator();
    protected abstract EditText getNameEditField();
    protected abstract String getAction();
    protected abstract void validateActions();
    protected abstract void enableWidgets(boolean nameFlag, boolean widgetFlag);
}
