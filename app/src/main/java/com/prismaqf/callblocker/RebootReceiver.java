package com.prismaqf.callblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Receiver to start the service after reboot
 * if it was running before
 * @author  ConteDiMonteCristo.
 */
public class RebootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(
                    context.getString(R.string.file_shared_prefs_name),
                    Context.MODE_PRIVATE);
            String state = prefs.getString(context.getString(R.string.shared_prefs_key_state), "not found");
            if (state.equals(context.getString(R.string.shared_prefs_state_running))) {
                Intent serviceIntent = new Intent(context, CallDetectService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
