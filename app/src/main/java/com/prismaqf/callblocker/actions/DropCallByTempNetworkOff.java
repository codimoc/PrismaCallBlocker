package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.LoggedCallProvider;
import com.prismaqf.callblocker.sql.ServiceRunProvider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Drop a call by temporarily siabling the mobile
 * network and then restoriting it back
 * @author ConteDiMonteCristo
 */
public class DropCallByTempNetworkOff implements IAction {

    private final String TAG = DropCallByTempNetworkOff.class.getCanonicalName();

    private final Context ctx;

    public DropCallByTempNetworkOff(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void act(final String number, final LogInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Dropping the network connection");
                try {
                    setMobileConnectionEnabled(false);
                } catch (Exception e) {
                    Log.e(TAG,e.getMessage());
                }
                Log.i(TAG, "Recording a call received in DB");
                SQLiteDatabase db = new DbHelper(ctx).getWritableDatabase();
                try {
                    //todo: get rule id
                    String contactDescription = resolveContactDescription(number);
                    LoggedCallProvider.LoggedCall lc = new LoggedCallProvider.LoggedCall(info.getRunId(),info.getRuleId(),number,contactDescription);
                    LoggedCallProvider.InsertRow(db, lc);
                    //todo: only numreceived is updated for the time being
                    ServiceRunProvider.UpdateWhileRunning(db, info.getRunId(), info.getNumReceived(), info.getNumTriggered());
                }
                finally {
                    db.close();
                }
                try {
                    setMobileConnectionEnabled(true);
                } catch (Exception e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        }).start();
    }

    private void setMobileConnectionEnabled(boolean enabled) throws ReflectiveOperationException {
        final ConnectivityManager mConnectivityManager = (ConnectivityManager)  ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class mClass = Class.forName(mConnectivityManager.getClass().getName());
        final Field mField = mClass.getDeclaredField("mService");
        mField.setAccessible(true);
        final Object mObject = mField.get(mConnectivityManager);
        final Class mConnectivityManagerClass =  Class.forName(mObject.getClass().getName());
        final Method setMobileDataEnabledMethod = mConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(mObject, enabled);
    }

    private String resolveContactDescription(String incomingNumber) {
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
