package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/**
 * Receiver to start the service after reboot
 * if it was running before
 * @author  ConteDiMonteCristo.
 */
public class RebootReceiver extends BroadcastReceiver{

    private static final String TAG = RebootReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Reboot receiver receiving");
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(
                    context.getString(R.string.file_shared_prefs_name),
                    Context.MODE_PRIVATE);
            String state = prefs.getString(context.getString(R.string.pk_state), "not found");
            if (state.equals(context.getString(R.string.tx_state_running))) {
                Intent serviceIntent = new Intent(context, CallDetectService.class);
                if (Build.VERSION.SDK_INT < 26)
                    context.startService(serviceIntent);
                else //changes with Android Oreo that otherwise throw and IllegalStateException
                    context.startForegroundService(serviceIntent);
                Log.i(TAG, "Starting CallDetectService after reboot completed");
            }
        }
    }
}
