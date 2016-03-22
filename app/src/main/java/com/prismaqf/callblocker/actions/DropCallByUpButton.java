package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * @author ConteDiMonteCristo
 * @see 'http://www.codeproject.com/Tips/578817/Reject-and-Accept-an-Incoming-Call'
 */
public class DropCallByUpButton implements IAction{
    private final static String TAG = DropCallByUpButton.class.getCanonicalName();
    private final static String DESCRIPTION = "Drop call by up button + headset hook";
    private final static String SHORT_DESCRIPTION = "Drop by up down";
    private final Context ctx;
    private final IAction logger;

    public DropCallByUpButton(Context ctx) {
        this.ctx = ctx;
        logger = new LogIncoming(ctx);
    }

    @Override
    public void act(String number, LogInfo info) {
        Log.i(TAG, "Dropping a call by up button");
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        ctx.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
        logger.act(number, info);
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

    @Override
    public String shortDescription() {
        return SHORT_DESCRIPTION;
    }
}
