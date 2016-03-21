package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.prismaqf.callblocker.actions.DropCallByDataConnectivity;
import com.prismaqf.callblocker.actions.DropCallByEndCall;
import com.prismaqf.callblocker.actions.DropCallByInvisibleActivity;
import com.prismaqf.callblocker.actions.IAction;
import com.prismaqf.callblocker.actions.LogIncoming;
import com.prismaqf.callblocker.actions.LogInfo;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.ServiceRunProvider;

/**
 * Helper class to detect incoming and outgoing calls
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */
public class CallHelper {

    private static final String TAG = CallHelper.class.getCanonicalName();

    private static CallHelper theHelper = null;

    private final Context ctx;
    private TelephonyManager tm;
    private final CallStateListener callListener;
    private final OutgoingReceiver outgoingReceiver;

    private synchronized void setNumReceived(int numReceived) {
        this.numReceived = numReceived;
    }

    private synchronized void setNumTriggered(int numTriggered) {
        this.numTriggered = numTriggered;
    }

    public int getNumReceived() {
        return numReceived;
    }

    public int getNumTriggered() {
        return numTriggered;
    }

    private int numReceived;
    private int numTriggered;
    private long myRunId;

    /**
     * Method to return the only intance of CallHelper (singleton)
     * @param ctx the Context, only needed when creating a CallHelper for the first time,
     *            a null value is ok all the remaining times
     * @return the single instance of a CallHelper class
     */
    public static CallHelper GetHelper(Context ctx) {
        if (theHelper==null) theHelper = new CallHelper(ctx);
        return theHelper;
    }


    /**
     * Listener to detect incoming calls
     */
    private class CallStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: //someone is ringing to this phone

                    //todo: only received is updated for the time being
                    setNumReceived(numReceived + 1);
                    Intent intent = new Intent();
                    intent.setAction(ctx.getString(R.string.ac_call));
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.putExtra(ctx.getString(R.string.ky_number_called), incomingNumber);
                    intent.putExtra(ctx.getString(R.string.ky_received),numReceived);
                    intent.putExtra(ctx.getString(R.string.ky_triggered), numTriggered);
                    ctx.sendBroadcast(intent);
                    Toast.makeText(ctx, "Incoming: " + incomingNumber, Toast.LENGTH_LONG).show();
                    LogIncoming action = new LogIncoming(ctx);
                    //todo: get rule id
                    //todo: only numreceived is updated for the time being
                    LogInfo info = new LogInfo();
                    info.setAll(myRunId, -1, numReceived, numTriggered);
                    /*if (incomingNumber.contains("523792")) {
                        //todo: adrop and below is clearly experimental. CHANGE
                        IAction adrop = new DropCallByInvisibleActivity(ctx);
                        adrop.act(incomingNumber,info);}
                    else*/
                        action.act(incomingNumber,info);
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
            Toast.makeText(ctx, "Outgoing: "+number, Toast.LENGTH_LONG).show();
        }
    }

    private CallHelper(Context ctx) {
        this.ctx = ctx;
        callListener = new CallStateListener();
        outgoingReceiver = new OutgoingReceiver();
        numReceived = 0;
        numTriggered = 0;
    }

    /**
     * Start calls detection
     */
    public void start() {
        Log.i(TAG, "Registering the listeners");
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
    }

    /**
     * Open a db connection and insert a record and set the run id
     */
    public void recordServiceStart() {
        Log.i(TAG, "Opening a DB connection and recording service start");
        SQLiteDatabase db = new DbHelper(ctx).getWritableDatabase();
        try {

            ServiceRunProvider.ServiceRun lastRun = ServiceRunProvider.LatestRun(db);
            setNumReceived(lastRun.getNumReceived());
            setNumTriggered(lastRun.getNumTriggered());
            if (lastRun.getId()==0 || lastRun.getStop() != null) {
                //new run
                myRunId = ServiceRunProvider.InsertAtServiceStart(db);
            } //otherwise the service was restarted and continue with old run
            else {
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
     */
    public void stop() {
        Log.i(TAG, "Unregistering the listeners");
        tm.listen(callListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(outgoingReceiver);
    }

    /**
     * Closing the DB connection and updating the service run record
     */
    public void recordServiceStop() {
        Log.i(TAG, "Closing the DB connection and updating the ServiceRunProvider record");
        SQLiteDatabase db = new DbHelper(ctx).getWritableDatabase();
        try {
            ServiceRunProvider.UpdateAtServiceStop(db, myRunId, numReceived, numTriggered);
        }
        finally {
            db.close();
        }
    }

    public static String resolveContactDescription(Context ctx, String incomingNumber) {
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


}
