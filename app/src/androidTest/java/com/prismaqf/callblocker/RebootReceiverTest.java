package com.prismaqf.callblocker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Test for receiver of BOOT_COMPLETED intent
 * @author ConteDiMonteCristo
 */
@RunWith(AndroidJUnit4.class)
public class RebootReceiverTest {

    private Context myContext;

    @Before
    public void before() {
        myContext = InstrumentationRegistry.getTargetContext();
        //set the service state to idle
        setServiceState(myContext.getString(R.string.shared_prefs_state_idle));
    }

    @After
    public void after() {
        //reset the state to idle
        setServiceState(myContext.getString(R.string.shared_prefs_state_idle));
    }


    @Test
    public void testIdleServiceWillNotStart() {
        //before the service is idle
        assertFalse("Service idle", CallBlockerManager.isServiceRunning(myContext));

        RebootReceiver receiver = new RebootReceiver();
        Intent reboot = new Intent("android.intent.action.BOOT_COMPLETED");
        receiver.onReceive(myContext,reboot);
        //after it is still idle
        assertFalse("Service idle", CallBlockerManager.isServiceRunning(myContext));
    }

    @Test
    public void testRunningServiceWillStartOnBoot() {
        //before the service is idle
        assertFalse("Service idle", CallBlockerManager.isServiceRunning(myContext));
        setServiceState(myContext.getString(R.string.shared_prefs_state_running));

        RebootReceiver receiver = new RebootReceiver();
        Intent reboot = new Intent("android.intent.action.BOOT_COMPLETED");
        receiver.onReceive(myContext,reboot);
        //after it is running
        assertTrue("Service running", CallBlockerManager.isServiceRunning(myContext));
    }

    private void setServiceState(String state) {
        SharedPreferences prefs = myContext.getSharedPreferences(
                myContext.getString(R.string.file_shared_prefs_name),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(myContext.getString(R.string.shared_prefs_key_state), state);
        editor.apply();
    }


}
