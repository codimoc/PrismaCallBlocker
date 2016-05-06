package com.prismaqf.callblocker;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import com.prismaqf.callblocker.sql.LoggedCallProvider;
import com.prismaqf.callblocker.utils.PreferenceHelper;

/**
 * Activity to show a list of recent triggered events
 * timestamp, calling number, description
 * and name of the action matched
 * @author ConteDiMonteCristo
 */
public class ShowTriggerEvents extends ShowLoggedCalls{

    private final String TAG = ShowTriggerEvents.class.getCanonicalName();
    private static final int URL_LOADER = 2; // Identifies a particular Loader being used in this component

    @Override
    public void initLoader() {
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

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
                        return LoggedCallProvider.LatestTriggered(myDbConnection, limit, true);
                    }
                };
            default:
                return null;
        }
    }
}
