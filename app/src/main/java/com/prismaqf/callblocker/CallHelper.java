package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.ServiceRun;

/**
 * Helper class to detect incoming and outgoing calls
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */
class CallHelper {

    private static final String TAG = CallHelper.class.getCanonicalName();

    private final Context ctx;
    private TelephonyManager tm;
    private final CallStateListener callListener;
    private final OutgoingReceiver outgoingReceiver;
    private int numReceived;
    private int numTriggered;
    private SQLiteDatabase myDb;
    private long myRunId;


    /**
     * Listener to detect incoming calls
     */
    private class CallStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: //someone is ringing to this phone

                    numReceived += 1;
                    Intent intent = new Intent();
                    intent.setAction(ctx.getString(R.string.action_call));
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.putExtra(ctx.getString(R.string.key_number_called), incomingNumber);
                    intent.putExtra(ctx.getString(R.string.key_received),numReceived);
                    intent.putExtra(ctx.getString(R.string.key_triggered),numTriggered);
                    ctx.sendBroadcast(intent);
                    Toast.makeText(ctx, "Incoming: " + incomingNumber, Toast.LENGTH_LONG).show();
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

    public CallHelper(Context ctx) {
        this.ctx = ctx;
        callListener = new CallStateListener();
        outgoingReceiver = new OutgoingReceiver();
        numReceived = 0;
        numTriggered = 0;
    }

    /**
     * Start calls detection
     * @param dbname the file path of the sqlite database
     */
    public void start(final String dbname) {
        Log.i(TAG,"Opening a DB connection");
        myDb = new DbHelper(ctx,dbname).getWritableDatabase();
        myRunId = ServiceRun.InsertAtServiceStart(myDb);

        Log.i(TAG, "Registering the listeners");
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
    }

    /**
     * Stop calls detection
     */
    public void stop() {
        Log.i(TAG, "Unregistering the listeners");
        tm.listen(callListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(outgoingReceiver);

        Log.i(TAG, "Updating DB");
        ServiceRun.UpdateAtServiceStop(myDb,myRunId,numReceived,numTriggered);
    }

}
