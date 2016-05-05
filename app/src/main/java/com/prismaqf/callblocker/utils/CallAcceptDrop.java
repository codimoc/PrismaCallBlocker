package com.prismaqf.callblocker.utils;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;


import java.io.IOException;

/**
 * @see 'http://stackoverflow.com/questions/26924618/how-can-incoming-calls-be-answered-programmatically-in-android-5-0-lollipop'
 */
public class CallAcceptDrop extends Activity {

    private static final String TAG = CallAcceptDrop.class.getCanonicalName();

    private static final String MANUFACTURER_HTC = "HTC";

    private KeyguardManager keyguardManager;
    private CallStateReceiver callStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();

        registerCallStateReceiver();
        updateWindowFlags();
        acceptCall();
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();

        if (callStateReceiver != null) {
            unregisterReceiver(callStateReceiver);
            callStateReceiver = null;
        }
    }

    private void registerCallStateReceiver() {
        Log.i(TAG,"registerCallStateReceiver");
        callStateReceiver = new CallStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callStateReceiver, intentFilter);
    }

    private void updateWindowFlags() {

        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            Log.i(TAG,"updateWindowsFlag:addFlags");
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            Log.i(TAG,"updateWindowsFlag:clearFlags");
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }

    private void acceptCall() {

        Log.i(TAG,"acceptCall");

        broadcastHeadsetConnected(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            try {
                Log.i(TAG, "execute input keycode headset hook");
                Runtime.getRuntime().exec("input keyevent " +
                        Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));

            } catch (IOException e) {
                // Runtime.exec(String) had an I/O problem, try to fall back
                Log.i(TAG,"send keycode headset hook intents");
                String enforcedPerm = "android.permission.CALL_PRIVILEGED";
                Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_HEADSETHOOK));
                Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                                KeyEvent.KEYCODE_HEADSETHOOK));

                sendOrderedBroadcast(btnDown, enforcedPerm);
                sendOrderedBroadcast(btnUp, enforcedPerm);
            }
        } finally {
            broadcastHeadsetConnected(false);
        }
    }

    private void broadcastHeadsetConnected(boolean connected) {
        Log.i(TAG,"broadcastHeadsetConnected");
        Intent i = new Intent(AudioManager.ACTION_HEADSET_PLUG);
        i.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        i.putExtra("state", connected ? 1 : 0);
        i.putExtra("name", "mysms");
        i.putExtra("microphone",1);
        try {
            sendOrderedBroadcast(i, null);
        } catch (Exception e) {
            Log.e(TAG,String.format("broadcastHeadsetConnected: %s",e.getMessage()));
        }
    }

    private class CallStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"onReceive");

            finish();
        }
    }
}