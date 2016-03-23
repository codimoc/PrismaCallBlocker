package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * Drop call for rooted phones
 * @author ConteDiMonteCristo
 */
@AvailableAction(description = "Drop for rooted phone (require grant)")
public class DropCallByRoot implements IAction {

    private final IAction logger;

    private final static String TAG = DropCallByRoot.class.getCanonicalName();
    private final static String DESCRIPTION = "Reject call for rooted phones (requires granting su privilege)";

    public DropCallByRoot(Context ctx) {
        logger = new LogIncoming(ctx);
    }

    @Override
    public void act(final String number, final LogInfo info) {
        Log.i(TAG, "Dropping a call using root");
        try {
            Runtime.getRuntime().exec(new String[]{"su","-c","input keyevent 6"});
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
        logger.act(number, info);
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

}
