package com.prismaqf.callblocker;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by ConteDiMonteCristo
 */
public class PermissionHelper {
    static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 102;

    static void checkWritingPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // permission wasn't granted
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }
    }
}
