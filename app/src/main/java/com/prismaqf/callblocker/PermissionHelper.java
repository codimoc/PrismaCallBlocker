package com.prismaqf.callblocker;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ConteDiMonteCristo
 */
class PermissionHelper {
    static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 102;
    static final int REQUEST_CODE_PERMISSION_TELEPHONY_STATE = 103;
    static final int REQUEST_CODE_PERMISSION_CONTACTS = 104;

    static void checkWritingPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    static void checkTelephonePermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PERMISSION_TELEPHONY_STATE);
        }
    }

    static void checkContactsPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_PERMISSION_CONTACTS);
        }
    }

    static void checkPermissions(Activity activity) {
        final boolean telephony = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
        final boolean contacts = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED;
        List<String> perms = new ArrayList<>();
        int code = 0;
        if (telephony) {
            code += REQUEST_CODE_PERMISSION_TELEPHONY_STATE;
            perms.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (contacts) {
            code += REQUEST_CODE_PERMISSION_CONTACTS;
            perms.add(Manifest.permission.READ_CONTACTS);
        }
        if (code > 0) {
            String[] arrPerrms = perms.toArray(new String[0]);
            ActivityCompat.requestPermissions(activity, arrPerrms, code);
        }


    }
}
