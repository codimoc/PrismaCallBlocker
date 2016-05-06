package com.prismaqf.callblocker;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.ServiceRunProvider;
import com.prismaqf.callblocker.utils.PreferenceHelper;

/**
 * Activity to show a list of recent service runs with
 * start time, end time and total number of call received
 * and event triggered
 * @author ConteDiMonteCristo
 */
public class ShowServiceRuns extends ShowListActivity {

    private final String TAG = ShowServiceRuns.class.getCanonicalName();
    private static final int URL_LOADER = 0; // Identifies a particular Loader being used in this component


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(this,   // Parent activity context
                                        null,   // All the following params
                                        null,   // are null
                                        null,   // because the Cursor is created
                                        null,   // below
                                        null)
                {
                    @Override
                    public Cursor loadInBackground() {
                        int limit = PreferenceHelper.GetSqlQueryLimit(getContext());
                        return ServiceRunProvider.LatestRuns(myDbConnection, limit);
                    }
                };
            default:
                return null;
        }
    }


    @Override
    public SimpleCursorAdapter getAdapter() {
        return new SimpleCursorAdapter(this,
                R.layout.service_run_record,
                null,  //no cursor yet
                new String[] {DbContract.ServiceRuns.COLUMN_NAME_START,
                        DbContract.ServiceRuns.COLUMN_NAME_STOP,
                        DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED,
                        DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED},
                new int[] {R.id.text_start_time,
                        R.id.text_end_time,
                        R.id.text_num_received,
                        R.id.text_num_triggered}, 0);
    }

    @Override
    public void initLoader() {
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }
}
