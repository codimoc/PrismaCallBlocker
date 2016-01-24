package com.prismaqf.callblocker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CallBlockerManager extends ActionBarActivity {

    private boolean detectEnabled;
    private TextView textDetectState;
    private Button buttonToggleDetect;
    private Button buttonExit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_blocker_manager);

        textDetectState = (TextView) findViewById(R.id.textDetectState);
        buttonToggleDetect = (Button) findViewById(R.id.buttonDetectToggle);
        buttonExit = (Button) findViewById(R.id.buttonExit);

        buttonToggleDetect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDetectEnabled(!detectEnabled);
            }
        });
        buttonExit.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDetectEnabled(false);
                finish();
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setDetectEnabled(boolean enable) {
        detectEnabled = enable;

        Intent intent = new Intent(this, CallDetectService.class);

        if (enable) {
            startService(intent);
            buttonToggleDetect.setText(R.string.turn_off);
            textDetectState.setText((R.string.detect));
        } else {
            stopService(intent);
            buttonToggleDetect.setText(R.string.turn_on);
            textDetectState.setText(R.string.no_detect);
        }
    }

}
