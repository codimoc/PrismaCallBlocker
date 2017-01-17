package com.prismaqf.callblocker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.prismaqf.callblocker.filters.Filter;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Fragment to manage the setiings
 * @author ConteDiMonteCristo
 */
public class SettingFragment extends PreferenceFragment{

    private static final String TAG = SettingFragment.class.getCanonicalName();

    private class ExportFilters extends AsyncTask<Context, Void, Void> {
        Context myContext;
        String myPath = "";

        @Override
        protected Void doInBackground(Context... ctxs) {
            myContext = ctxs[0];
            Log.i(TAG,"Exporting the filters");
            try
            {
                File file = new File(getStorageDir(getString(R.string.export_dirpath)), getString(R.string.export_filename));
                myPath = file.getAbsolutePath();
                FileOutputStream fOut = new FileOutputStream(file);
                ObjectOutputStream oOut = new ObjectOutputStream(fOut);
                oOut.writeObject(CallHelper.GetHelper().getFilters(myContext));
                oOut.flush();
                oOut.close();

            }
            catch (IOException e)
            {
                Log.e(TAG, "Filters' serialization failed: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (myContext==null) return;
            String msg = String.format("Serialized the filters to %s",myPath);
            Log.i(TAG, msg);
            Toast.makeText(myContext,msg,Toast.LENGTH_SHORT).show();
        }
    }

    private class ImportFilters extends AsyncTask<Context, Void, Void> {

        Context myContext;
        String myPath = "";

        @Override
        protected Void doInBackground(Context... ctxs) {
            myContext = ctxs[0];
            Log.i(TAG,"Importing the filters");
            SQLiteDatabase db = null;
            try
            {
                File file = new File(getStorageDir(getString(R.string.export_dirpath)), getString(R.string.export_filename));
                myPath = file.getAbsolutePath();
                FileInputStream fIn = new FileInputStream(file);
                ObjectInputStream oIn = new ObjectInputStream(fIn);
                List<Filter> filters=(List<Filter>) oIn.readObject();
                if (filters == null)
                    throw new Exception("Could not deserialize file into a list of filters");
                db = new DbHelper(myContext).getWritableDatabase();
                for (Filter f : filters)
                    FilterProvider.SaveFilter(db,f);
                CallHelper.GetHelper().loadFilters(myContext);
                oIn.close();

            }
            catch (Exception e)
            {
                Log.e(TAG, "Filters' deserialization failed: " + e.getMessage());
            }
            finally {
                if (db != null)
                    db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (myContext==null) return;
            String msg = String.format("Deserialized the filters from %s",myPath);
            Log.i(TAG, msg);
            Toast.makeText(myContext,msg,Toast.LENGTH_SHORT).show();
        }
    }

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
                    new ExportFilters().execute(getActivity());
                    return true;
                }
            });
        }
        Preference importer = findPreference(getString(R.string.pk_imp_rules));
        File rules = new File(getStorageDir(getString(R.string.export_dirpath)), getString(R.string.export_filename));
        if (!isExternalStorageReadable() || !isExternalStorageWritable() || !rules.exists())
            importer.setEnabled(false);
        else {
            importer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ImportFilters().execute(getActivity());
                    return true;
                }
            });
        }
        Preference shProtected = findPreference(getString(R.string.px_show_protected));
        shProtected.setEnabled(false);
        final SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.file_shared_prefs_name),
                Context.MODE_PRIVATE);
        Boolean skipShowProtected = prefs.getBoolean(getString(R.string.pk_skip_protected), false);
        if (CallBlockerManager.isHuawei(getActivity()) && skipShowProtected){
            shProtected.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    CallBlockerManager.HuaweiAlert(getActivity(),true);
                    return true;
                }
            });
            shProtected.setEnabled(true);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(getString(R.string.pk_skip_protected),false);
            editor.apply();
        }
        Preference help = findPreference(getString(R.string.mn_help));
        if (help != null)
            help.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showHelp();
                    return true;
                }
            });
    }

    private void showHelp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(R.string.tx_settings);

        WebView wv = new WebView(getActivity());
        wv.loadUrl("file:///android_asset/html/settings.html");
        ScrollView scroll = new ScrollView(getActivity());
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scroll.addView(wv);

        alert.setView(scroll);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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
