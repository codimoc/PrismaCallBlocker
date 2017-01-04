package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Receiver to detect the state of the telephone ringing and ensure
 * the service is running
 * @author ConteDiMonteCristo
 */

public class TelephonyStateReceiver extends BroadcastReceiver {

    private static final String TAG = TelephonyStateReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receiving a call..");
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(
                    context.getString(R.string.file_shared_prefs_name),
                    Context.MODE_PRIVATE);
            String state = prefs.getString(context.getString(R.string.pk_state), "not found");
            if (state.equals(context.getString(R.string.tx_state_running)) &&
                CallBlockerManager.isServiceRunning(context)) {
                Intent serviceIntent = new Intent(context, CallDetectService.class);
                context.startService(serviceIntent);
                Log.i(TAG, "Starting CallDetectService after dozing");
            }
        }

    }
}
