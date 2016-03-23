package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.widget.Toast;

/**
 * Just warn the user of an incoming call that has triggered an event
 * @author ConteDiMonteCristo
 */
@AvailableAction(description = "Warn users of a triggered event")
public class WarnTrigger implements IAction {
    private final static String TAG = DropCallByDataConnectivity.class.getCanonicalName();
    private final static String DESCRIPTION = "Warn users of a triggered event";

    private final IAction logger;
    private final Context ctx;

    public WarnTrigger(Context ctx) {
        logger = new LogIncoming(ctx);
        this.ctx = ctx;
    }

    @Override
    public void act(String number, LogInfo info) {
        Toast.makeText(ctx, String.format("Suspected number %s has triggered an event. Reject call?",number), Toast.LENGTH_LONG).show();
        logger.act(number,info);
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }


}
