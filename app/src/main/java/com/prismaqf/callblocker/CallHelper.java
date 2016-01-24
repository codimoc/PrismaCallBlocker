package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Helper class to detect incoming and outgoing calls
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */
public class CallHelper {

    private Context ctx;
    private TelephonyManager tm;
    private CallStateListener callListener;
    private OutgoingReceiver outgoingReceiver;


    /**
     * Listener to detect incoming calls
     */
    private class CallStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: //someone is ringing to this phone

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
    }

    /**
     * Start calls detection
     */
    public void start() {
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callListener,PhoneStateListener.LISTEN_CALL_STATE);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        ctx.registerReceiver(outgoingReceiver, intentFilter);
    }

    /**
     * Stop calls detection
     */
    public void stop() {
        tm.listen(callListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(outgoingReceiver);
    }

}
