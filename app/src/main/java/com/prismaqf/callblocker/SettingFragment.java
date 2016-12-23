package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Fragment to manage the setiings
 * @author ConteDiMonteCristo
 */
public class SettingFragment extends PreferenceFragment{

    private static final String TAG = SettingFragment.class.getCanonicalName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PermissionHelper.checkWritingPermission(getActivity());

        Preference exporter = findPreference(getString(R.string.pk_exp_rules));
        if (!isExternalStorageReadable() || !isExternalStorageWritable())
            exporter.setEnabled(false);
        else {
            exporter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try
                    {
                        // Create the file.
                        File file = new File(getStorageDir(getString(R.string.export_dirpath)), getString(R.string.export_filename));
                        FileOutputStream fOut = new FileOutputStream(file);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append("Hello world!");

                        myOutWriter.close();

                        fOut.flush();
                        fOut.close();
                    }
                    catch (IOException e)
                    {
                        Log.e(TAG, "File write failed: " + e.getMessage());
                    }
                    return true;
                }
            });
        }
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private File getStorageDir(String folder) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),folder);
        file.mkdirs();
        return file;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionHelper.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Write on External Storage Permission granted");
            } else {
                Log.i(TAG, "Write on External Storage Permission not granted");
            }
        }
    }


}
