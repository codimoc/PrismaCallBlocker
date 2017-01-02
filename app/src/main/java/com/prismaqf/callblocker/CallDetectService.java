package com.prismaqf.callblocker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Call detect service
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */public class CallDetectService extends Service {

    private static final String TAG = CallDetectService.class.getCanonicalName();
    private static final int ONGOING_NOTIFICATION_ID = 1007;
    private PowerManager.WakeLock myWakeLock;


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        CallDetectService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CallDetectService.this;
        }
    }

    private final CallHelper myCallHelper;
    private final IBinder myBinder = new LocalBinder();
    
    public CallDetectService() {
        myCallHelper = CallHelper.GetHelper(this);
    }

    public int getNumReceived() {return myCallHelper.getNumReceived();}

    public int getNumTriggered() {return myCallHelper.getNumTriggered();}
    

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        /*
        The idea of starting the service in the foreground + aquiring a wake lock
        does not work properly in Marshmallow. The service is still killed by the
        doze mode. Untill this is fixed the only solution is to add the app to
        the whitelist to prevent doze
         */
        myCallHelper.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (myCallHelper) {
                    myCallHelper.recordServiceStart();
                }
            }
        }).start();


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, CallBlockerManager.class), 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.tx_notification))
                .setSmallIcon(R.drawable.police)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.app_name))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        myWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        myWakeLock.acquire();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG,"The service has been killed");
/*        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    myCallHelper.recordServiceStop();
                }
            }
        }).start();*/
        if (myWakeLock!=null)
            myWakeLock.release();
        myCallHelper.recordServiceStop();
        myCallHelper.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

}
