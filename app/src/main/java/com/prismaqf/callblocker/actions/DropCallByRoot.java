package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

/**
 * Drop call for rooted phones
 * @author ConteDiMonteCristo
 */
@AvailableAction(description = "Drop for rooted phone (require grant)")
public class DropCallByRoot implements IAction, Serializable {
    private final static String TAG = DropCallByRoot.class.getCanonicalName();
    private final static String DESCRIPTION = "Reject call for rooted phones (requires granting su privilege)";
    private final static long serialVersionUID = 1L; //for serialization consistency

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public void act(final Context ctx, final String number, final LogInfo info) {
        Log.i(TAG, "Dropping a call using root");
        try {
            Runtime.getRuntime().exec(new String[]{"su","-c","input keyevent 6"});
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

}
