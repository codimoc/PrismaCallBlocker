package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Just warn the user of an incoming call that has triggered an event
 * @author ConteDiMonteCristo
 */
@AvailableAction(description = "Warn users of a triggered event")
public class WarnTrigger implements IAction, Serializable {
    private final static String TAG = DropCallByDataConnectivity.class.getCanonicalName();
    private final static String DESCRIPTION = "Warn users of a triggered event";
    private final static long serialVersionUID = 1L; //for serialization consistency

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public void act(Context ctx, String number, LogInfo info) {
        Toast.makeText(ctx, String.format("Suspected number %s has triggered an event. Reject call?",number), Toast.LENGTH_LONG).show();
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }


}
