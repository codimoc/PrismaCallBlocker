package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.prismaqf.callblocker.actions.LogIncoming;
import com.prismaqf.callblocker.actions.LogInfo;
import com.prismaqf.callblocker.filters.Filter;
import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;
import com.prismaqf.callblocker.sql.ServiceRunProvider;
import com.prismaqf.callblocker.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to detect incoming and outgoing calls
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */
public class CallHelper {

    private static final String TAG = CallHelper.class.getCanonicalName();

    private static CallHelper theHelper = null;
    private static boolean isRunning = false;

    private TelephonyManager tm;
    private CallStateListener callListener;
    private OutgoingReceiver outgoingReceiver;

    private synchronized void setNumReceived(int numReceived) {
        this.numReceived = numReceived;
    }

    private synchronized void setNumTriggered(int numTriggered) {
        this.numTriggered = numTriggered;
    }

    int getNumReceived() {
        return numReceived;
    }

    int getNumTriggered() {
        return numTriggered;
    }

    private int numReceived;
    private int numTriggered;
    private long myRunId;
    private List<Filter> myFilters = new ArrayList<>();

    /**
     * Method to return the only intance of CallHelper (singleton)
     * @return the single instance of a CallHelper class
     */
    static CallHelper GetHelper() {
        if (theHelper==null) theHelper = new CallHelper();
        return theHelper;
    }

    /**
     * Check if the serice is running
     * @return a boolean flag to indicate if the service is running
     */
    static boolean IsRunning() { return isRunning;}


    /**
     * Listener to detect incoming calls
     */
    private class CallStateListener extends PhoneStateListener {

        private Context ctx;

        CallStateListener(Context context) {
            super();
            ctx = context;
        }

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: //someone is ringing to this phone
                    LogInfo info = new LogInfo();
                    info.setAll(myRunId,numReceived+1, numTriggered, null);
                    boolean logging = false;
                    for (Filter f : myFilters) {
                        f.act(ctx,incomingNumber,info);
                        if (f.getAction().getName().equals(LogIncoming.class.getCanonicalName()))
                            logging = true;
                    }
                    setNumReceived(info.getNumReceived());
                    setNumTriggered(info.getNumTriggered());
                    Intent intent = new Intent();
                    intent.setAction(ctx.getString(R.string.ac_call));
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.putExtra(ctx.getString(R.string.ky_number_called), incomingNumber);
                    intent.putExtra(ctx.getString(R.string.ky_received),getNumReceived());
                    intent.putExtra(ctx.getString(R.string.ky_triggered), getNumTriggered());
                    ctx.sendBroadcast(intent);
                    if (PreferenceHelper.GetToastVerbosity(ctx) > 0)
                        Toast.makeText(ctx, "Incoming: " + incomingNumber, Toast.LENGTH_LONG).show();
                    if (!logging) {
                        new LogIncoming().act(ctx,incomingNumber,info);
                    }
                    break;
            }
        }
    }

    /**
     * Broadcast receiver to detect outgoing calls
     */
    public class OutgoingReceiver extends BroadcastReceiver {

        public OutgoingReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (PreferenceHelper.GetToastVerbosity(context) > 1)
                Toast.makeText(context, "Outgoing: "+number, Toast.LENGTH_LONG).show();
        }
    }

    private class LoadFilters extends AsyncTask<Context, Void, Void> {

        Context myContext;
        List<Filter> filters = new ArrayList<>();

        List<Filter> getFilters() {
            return filters;
        }

        @Override
        protected Void doInBackground(Context... ctxs) {
            myContext = ctxs[0];
            Log.i(TAG,"Loading the filters");
            SQLiteDatabase db = null;
            try {
                db = new DbHelper(myContext).getReadableDatabase();
                List<FilterHandle> handles = FilterProvider.LoadFilters(db);
                for(FilterHandle h : handles)
                    filters.add(Filter.makeFilter(myContext,h));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                String msg = filters.size() > 1 ?
                        String.format(Locale.getDefault(),"%d filters loaded", filters.size()):
                        String.format(Locale.getDefault(),"%d filter loaded", filters.size());
                Log.i(TAG,msg);
                if (db != null) db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void v) {
            if (myContext==null) return;
            String msg = filters.size() > 1 ?
                    String.format(Locale.getDefault(),"%d filters loaded", filters.size()):
                    String.format(Locale.getDefault(),"%d filter loaded", filters.size());
            if (PreferenceHelper.GetToastVerbosity(myContext) > 1)
                Toast.makeText(myContext, msg, Toast.LENGTH_LONG).show();
            myFilters = filters;
        }
    }

    private class PurgeLogs extends AsyncTask<Context, Void, Integer> {

        Context myContext;

        @Override
        protected Integer doInBackground(Context... ctxs) {
            myContext = ctxs[0];
            Log.i(TAG,"Purging the logs");
            SQLiteDatabase db = null;
            int purged = 0;
            try {
                db = new DbHelper(myContext).getWritableDatabase();
                purged = ServiceRunProvider.PurgeLog(db,myContext,null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                String msg = purged > 0  ?
                        String.format(Locale.getDefault(),"%d service run records purged", purged):
                        "No service run records purged";
                Log.i(TAG,msg);
                if (db != null) db.close();
            }
            return purged;
        }

        @Override
        protected void onPostExecute (Integer purged) {
            if (myContext==null) return;
            String msg = purged > 0  ?
                    String.format(Locale.getDefault(),"%d service run records purged", purged):
                    "No service run records purged";
            if (PreferenceHelper.GetToastVerbosity(myContext) > 1)
                Toast.makeText(myContext, msg, Toast.LENGTH_LONG).show();
        }
    }

    private CallHelper() {
        numReceived = 0;
        numTriggered = 0;
    }

    /**
     * Start calls detection
     * @param ctx the context
     */
    public void start(Context ctx) {
        if (isRunning) {
            Log.e(TAG,"The service is already running");
            if (PreferenceHelper.GetToastVerbosity(ctx) > 0)
                Toast.makeText(ctx,"The service is already running",Toast.LENGTH_LONG).show();
            return;
        }
        if (callListener==null)
            callListener = new CallStateListener(ctx);
        if (outgoingReceiver==null)
            outgoingReceiver = new OutgoingReceiver();
        purgeLogs(ctx);
        loadFilters(ctx);
        Log.i(TAG, "Registering the listeners");
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
        isRunning = true;
    }

    /**
     * Open a db connection and insert a record and set the run id
     * @param ctx the context
     */
    void recordServiceStart(Context ctx) {
        Log.i(TAG, "Opening a DB connection and recording service start");
        SQLiteDatabase db = new DbHelper(ctx).getWritableDatabase();
        try {
            ServiceRunProvider.ServiceRun lastRun = ServiceRunProvider.LatestRun(db);
            if (lastRun.getId()==0 || lastRun.getStop() != null) {
                //new run
                setNumReceived(0);
                setNumTriggered(0);
                myRunId = ServiceRunProvider.InsertAtServiceStart(db);
            } //otherwise the service was restarted and continue with old run
            else {
                setNumReceived(lastRun.getNumReceived());
                setNumTriggered(lastRun.getNumTriggered());
                myRunId = lastRun.getId();
            }
            ServiceRunProvider.UpdateWhileRunning(db, myRunId, -1, -1);
        }
        finally {
            db.close();
        }
    }

    /**
     * Stop calls detection
     * @param ctx the context
     */
    void stop(Context ctx) {
        isRunning = false;
        Log.i(TAG, "Unregistering the listeners");
        if (callListener!= null)
            tm.listen(callListener, PhoneStateListener.LISTEN_NONE);
        if (outgoingReceiver!=null)
            ctx.unregisterReceiver(outgoingReceiver);
        setNumTriggered(0);
        setNumReceived(0);
    }

    /**
     * Closing the DB connection and updating the service run record
     * @param ctx the context
     */
    void recordServiceStop(Context ctx) {
        Log.i(TAG, "Closing the DB connection and updating the ServiceRunProvider record");
        SQLiteDatabase db = new DbHelper(ctx).getWritableDatabase();
        ServiceRunProvider.ServiceRun lastRun = ServiceRunProvider.LatestCompletedRun(db);
        try {
            ServiceRunProvider.UpdateAtServiceStop(db, myRunId,
                                                   numReceived + lastRun.getNumReceived(),
                                                   numTriggered + lastRun.getNumTriggered());
        }
        finally {
            db.close();
        }
    }

    public static String resolveContactDescription(Context ctx, String incomingNumber) {
        if (incomingNumber==null || incomingNumber.isEmpty() || incomingNumber.toLowerCase().contains("private"))
            return "private number";
        String description = "Not found";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String[] projection = new String[]{ ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor c = ctx.getContentResolver().query(uri, projection, null, null, null);
        if (c!= null && c.getCount()>0) {
            c.moveToFirst();
            description = c.getString(0);
        }
        if (c != null) {
            c.close();
        }

        return description;
    }

    /**
     * This is an asynchronous loader of filters.
     * The CallHelper filters are set when the thread terminates (onPostExecute)
     * @param context the context
     */
    void loadFilters(final Context context) {
        LoadFilters lf = new LoadFilters();
        lf.execute(context);
    }

    /**
     * This is an synchronous getter of filters.
     * @param context the context
     * @return the filters
     */
    List<Filter> getFilters(final Context context) {
        List<Filter> filters = new ArrayList<>();
        Log.i(TAG,"Getting the filters");
        SQLiteDatabase db = null;
        try {
            db = new DbHelper(context).getReadableDatabase();
            List<FilterHandle> handles = FilterProvider.LoadFilters(db);
            for(FilterHandle h : handles)
                filters.add(Filter.makeFilter(context,h));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            String msg = filters.size() > 1 ?
                    String.format(Locale.getDefault(),"%d filters loaded", filters.size()):
                    String.format(Locale.getDefault(),"%d filter loaded", filters.size());
            Log.i(TAG,msg);
            if (db != null) db.close();
        }
        return filters;
    }

    private void purgeLogs(final Context context) {
        new PurgeLogs().execute(context);
    }


}
