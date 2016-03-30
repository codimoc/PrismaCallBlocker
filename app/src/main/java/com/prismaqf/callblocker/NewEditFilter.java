package com.prismaqf.callblocker;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.EditText;

import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;

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
    protected void validateActions() {

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
    protected void enableWidgets(boolean nameFlag, boolean widgetFlag) {

    }
}
