package com.prismaqf.callblocker;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.ListAdapter;

import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.ServiceRun;

/**
 * Activity to show a list of recent service runs with
 * start time, end time and total number of call received
 * and event triggered
 * @author ConteDiMonteCristo
 */
public class ShowServiceRuns extends ListActivity {

    private final String TAG = ShowServiceRuns.class.getCanonicalName();
    private CallDetectService myService;
    private boolean isBound = false;
    private SQLiteDatabase myDb;


    private ServiceConnection myConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CallDetectService.LocalBinder binder = (CallDetectService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_runs_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG,"Retrieving the recent history of service runs");
        //todo: do this in a separate thread (AsyncTask)
        myDb = new DbHelper(this).getReadableDatabase();
        //todo: get the limit from shared preferences
        Cursor c = ServiceRun.LatestRuns(myDb,10);
        //todo: fix deprecated
        ListAdapter adapter = new SimpleCursorAdapter(this,R.layout.service_run_record,c,
                                                      new String[] {DbContract.ServiceRuns.COLUMN_NAME_START,
                                                              DbContract.ServiceRuns.COLUMN_NAME_STOP,
                                                              DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED,
                                                              DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED},
                                                      new int[] {R.id.text_start_time,
                                                                 R.id.text_end_time,
                                                                 R.id.text_num_received,
                                                                 R.id.text_num_triggered});
        setListAdapter(adapter);
        //end of todos

    }

    @Override
    protected void onStop() {
        super.onStop();
        myDb.close();
    }
}
