package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.LoggedCallProvider;
import com.prismaqf.callblocker.sql.ServiceRunProvider;

import java.lang.reflect.Method;

/**
 * Action to drop a call using a ITelephony stub
 * @author ConteDiMonteCristo
 * @see 'http://androidsourcecode.blogspot.co.uk/2010/10/blocking-incoming-call-android.html'
 */
public class DropCallByITelephony implements IAction{

    private final String TAG = DropCallByITelephony.class.getCanonicalName();

    private final Context ctx;

    public DropCallByITelephony(Context ctx) {
        this.ctx = ctx;
    }


    @Override
    public void act(final String number, final LogInfo info) {
        //now store to DB
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Dropping a call using Telephony service");
                TelephonyManager telephony = (TelephonyManager)
                        ctx.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Class c = Class.forName(telephony.getClass().getName());
                    Method m = c.getDeclaredMethod("getITelephony");
                    m.setAccessible(true);
                    ITelephony telephonyService = (ITelephony) m.invoke(telephony);
                    //telephonyService.silenceRinger();
                    telephonyService.endCall();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
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
            }
        }).start();
    }

    private String resolveContactDescription(String incomingNumber) {
        String description = "Not found";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String[] projection = new String[]{ ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor c = ctx.getContentResolver().query(uri,projection,null,null,null);
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
