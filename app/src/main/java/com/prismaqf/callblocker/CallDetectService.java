package com.prismaqf.callblocker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Call detect service
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */public class CallDetectService extends Service {
    
    private CallHelper callHelper;
    
    public CallDetectService() {}
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        callHelper = new CallHelper(this);

        int res = super.onStartCommand(intent,flags,startId);
        callHelper.start();
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callHelper.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //not supporting binding
        return null;
    }
}
