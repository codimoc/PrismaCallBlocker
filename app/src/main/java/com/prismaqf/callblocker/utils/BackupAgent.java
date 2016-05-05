package com.prismaqf.callblocker.utils;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.prismaqf.callblocker.sql.DbHelper;


/**
 * BackupAgent class for cloud backup service
 * Created by ConteDiMonteCriso
 */
public class BackupAgent extends BackupAgentHelper {

    private static final String TAG = BackupAgent.class.getCanonicalName();

    class DbBackupHelper extends FileBackupHelper{

        public DbBackupHelper(Context context, String... files) {
            super(context, files);
        }

        @Override
        public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
            Log.i(TAG,"Backing up the database");
            synchronized (DbHelper.getDbHelperLock()) {
                super.performBackup(oldState, data, newState);
            }
        }

        @Override
        public void restoreEntity(BackupDataInputStream data) {
            Log.i(TAG,"Restoring the database");
            synchronized (DbHelper.getDbHelperLock()) {
                super.restoreEntity(data);
            }
        }
    }

    class PrefBackupHelper extends SharedPreferencesBackupHelper{

        public PrefBackupHelper(Context context, String... prefGroups) {
            super(context, prefGroups);
        }

        @Override
        public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
            Log.i(TAG,"Backing up the shared preferences");
            super.performBackup(oldState, data, newState);
        }

        @Override
        public void restoreEntity(BackupDataInputStream data) {
            Log.i(TAG,"Restoring the shared preferences");
            super.restoreEntity(data);
        }
    }


    @Override
    public void onCreate(){
        String dbname = "../databases/pcb.db";
        DbBackupHelper dbs = new DbBackupHelper(this, dbname);
        String DBKEY = "dbs";
        addHelper(DBKEY, dbs);
        String preferences = "com.prismaqf.callblocker_preferences";
        PrefBackupHelper prefs = new PrefBackupHelper(this, preferences);
        String PREFKEY = "prefs";
        addHelper(PREFKEY,prefs);
    }

}
