package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Action to drop a call using a ITelephony stub
 * @author ConteDiMonteCristo
 * @see 'http://androidsourcecode.blogspot.co.uk/2010/10/blocking-incoming-call-android.html'
 */
@AvailableAction(description = "Drop by Itelephone endCall (preferrred)")
public class DropCallByEndCall implements IAction, Serializable{

    private final static String TAG = DropCallByEndCall.class.getCanonicalName();
    private final static String DESCRIPTION = "Use ITelephony to end call (preferred action)";
    private final static long serialVersionUID = 1L; //for serialization consistency


    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public void act(final Context ctx, final String number, final LogInfo info) {

        Log.i(TAG, "Dropping a call using Telephony service");
        TelephonyManager telephony = (TelephonyManager)
                ctx.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> c = Class.forName(telephony.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(telephony);
            //telephonyService.silenceRinger();
            telephonyService.endCall();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

}
