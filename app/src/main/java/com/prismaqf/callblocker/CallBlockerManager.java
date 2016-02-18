package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.ServiceRun;

public class CallBlockerManager extends ActionBarActivity {

    private static final String TAG = CallBlockerManager.class.getCanonicalName();

    private TextView textDetectState;
    private CallEventReceiver callEventReceiver;
    private Button buttonReceived;
    private Button buttonTriggered;
    private CallDetectService myService;
    private boolean isBound;

    private ServiceConnection myConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            CallDetectService.LocalBinder binder = (CallDetectService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            buttonReceived.setText(String.valueOf(myService.getNumReceived()));
            buttonReceived.invalidate();
            buttonTriggered.setText(String.valueOf(myService.getNumTriggered()));
            buttonTriggered.invalidate();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    /**
     * Broadcast receiver to receive intents when a call is detected
     */
    private class CallEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String number = intent.getStringExtra(context.getString(R.string.key_number_called));
            int numReceived = intent.getIntExtra(context.getString(R.string.key_received), 0);
            int numTriggered = intent.getIntExtra(context.getString(R.string.key_triggered), 0);
            String message = String.format("Incoming: %s, Num received: %d, Num triggered: %d",
                                           number, numReceived, numTriggered);
            Log.i(TAG,message);

            if (buttonReceived == null)
                buttonReceived = (Button) findViewById(R.id.button_received);
            if (buttonTriggered == null)
                buttonTriggered = (Button) findViewById(R.id.button_triggered);

            buttonReceived.setText(String.valueOf(numReceived));
            buttonReceived.invalidate();
            buttonTriggered.setText(String.valueOf(numTriggered));
            buttonTriggered.invalidate();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_blocker_manager);

        textDetectState = (TextView) findViewById(R.id.textDetectState);
        ToggleButton buttonToggleDetect = (ToggleButton) findViewById(R.id.buttonDetectToggle);
        if (isServiceRunning(this)) {
            textDetectState.setText(R.string.detect);
            buttonToggleDetect.setChecked(true);
        }
        Button buttonExit = (Button) findViewById(R.id.buttonExit);

        buttonToggleDetect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDetectEnabled();
            }
        });
        buttonExit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //call receiver
        callEventReceiver = new CallEventReceiver();
        IntentFilter filter = new IntentFilter(getString(R.string.action_call));
        registerReceiver(callEventReceiver,filter);

        //call stats buttons
        buttonReceived = (Button) findViewById(R.id.button_received);
        buttonTriggered = (Button) findViewById(R.id.button_triggered);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isServiceRunning(this)) {
            Intent intent = new Intent(this, CallDetectService.class);
            bindService(intent, myConnection, Context.BIND_ABOVE_CLIENT);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound){
            unbindService(myConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(callEventReceiver);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_call_blocker_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_show_runs:
                showRuns();
                return true;
            case R.id.action_show_calls:
                showCalls();
                return true;
            case R.id.action_show_calendar_rules:
                showCalendarRules();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setDetectEnabled() {

        if (!isServiceRunning(this)) {
            startService();
            //save the running state in a shared preference file
            Context ctx = getApplicationContext();
            SharedPreferences prefs = ctx.getSharedPreferences(
                    getString(R.string.file_shared_prefs_name),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.shared_prefs_key_state),
                    getString(R.string.shared_prefs_state_running));
            editor.apply();
        } else {
            stopService();
            //save the idle state in a shared preference file
            Context ctx = getApplicationContext();
            SharedPreferences prefs = ctx.getSharedPreferences(
                    getString(R.string.file_shared_prefs_name),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.shared_prefs_key_state),
                    getString(R.string.shared_prefs_state_idle));
            editor.apply();
        }
    }

    private void stopService() {
        if (isBound) {
            unbindService(myConnection);
            isBound = false;
        }
        Log.i(TAG, "Stopping the service");
        Intent intent = new Intent(this, CallDetectService.class);
        stopService(intent);
        textDetectState.setText(R.string.no_detect);

    }

    private void startService() {
        Log.i(TAG, "Starting the service");
        Intent intent = new Intent(this, CallDetectService.class);
        startService(intent);
        textDetectState.setText((R.string.detect));
        //todo: might need to use an async task
        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        ServiceRun last = ServiceRun.LatestRun(db);
        buttonReceived.setText(String.valueOf(last.getNumReceived()));
        buttonTriggered.setText(String.valueOf(last.getNumTriggered()));
    }


    public static boolean isServiceRunning(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().equals(CallDetectService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    private void showRuns() {
        Intent intent = new Intent(this,ShowServiceRuns.class);
        startActivity(intent);
    }

    private void showCalls() {
        Intent intent = new Intent(this,ShowLoggedCalls.class);
        startActivity(intent);
    }

    private void showCalendarRules() {
        Intent intent = new Intent(this,EditCalendarRules.class);
        startActivity(intent);
    }


    private void showSettings() {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
    }

}
