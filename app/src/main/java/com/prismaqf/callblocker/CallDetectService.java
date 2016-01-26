package com.prismaqf.callblocker;

import android.app.Notification;
import android.app.PendingIntent;
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


        //experimental
        //TODO: substitute hard coded values and check deprecated
        int ONGOING_NOTIFICATION_ID = 1234;
        Notification notification = new Notification(R.mipmap.ic_launcher,getText(R.string.app_name),System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, CallDetectService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, "Prisma Call Blocker service","Prisma Call Blocker service", pendingIntent);
        startForeground(ONGOING_NOTIFICATION_ID, notification);//experimental

        //super.onStartCommand(intent,flags,startId);
        callHelper.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        callHelper.stop();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //not supporting binding
        return null;
    }
}
