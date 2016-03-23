package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Drop a call by temporarily disabling the mobile
 * network connectivity and then restoriting it back
 * @author ConteDiMonteCristo
 */
public class DropCallByDataConnectivity implements IAction {

    private final static String TAG = DropCallByDataConnectivity.class.getCanonicalName();
    private final static String DESCRIPTION = "Drop call by switching off data connectivity (requires special permission)";

    private final IAction logger;
    private final Context ctx;

    public DropCallByDataConnectivity(Context ctx) {
        logger = new LogIncoming(ctx);
        this.ctx = ctx;
    }

    @Override
    public void act(final String number, final LogInfo info) {
        Log.i(TAG, "Dropping the network connection");
        try {
            setMobileConnectionEnabled(false);
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
        logger.act(number, info);
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

    private void setMobileConnectionEnabled(boolean enabled) throws ReflectiveOperationException {
        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        Class telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
        Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
        getITelephonyMethod.setAccessible(true);
        Object ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
        Class ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
        Method dataConnSwitchmethod;
        if (enabled)
            dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
        else
            dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");

        dataConnSwitchmethod.setAccessible(true);
        dataConnSwitchmethod.invoke(ITelephonyStub);
    }
}
