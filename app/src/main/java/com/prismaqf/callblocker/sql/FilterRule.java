package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author ConteDiMonteCristo
 */
public class FilterRule {
    private static final String TAG = FilterRule.class.getCanonicalName();

    private final long id;
    private final String name;
    private final String description;

    private FilterRule(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static FilterRule deserialize(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(DbContract.FilterRules._ID));
        String name = c.getString(c.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_RULENAME));
        String description = c.getString(c.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_DESCRIPTION));
        return new FilterRule(id,name,description);
    }

    public static void serialize(SQLiteDatabase db, FilterRule fr) {
        InsertRow(db,fr.getName(),fr.getDescription());
    }

    /**
     * Insert a FilterRule
     * @param db the SQLite connection
     * @param name the name of the rule
     * @param description the description (optional)
     * @return the rule id
     */
    public static long InsertRow(SQLiteDatabase db, String name, String description) {
        ContentValues vals = new ContentValues();
        if (name == null) {
            final String msg = "Rule name is required for a filter rule";
            Log.e(TAG, msg);
            throw new SQLException(msg);
        }
        vals.put(DbContract.FilterRules.COLUMN_NAME_RULENAME,name);
        if (description != null)
            vals.put(DbContract.FilterRules.COLUMN_NAME_DESCRIPTION,description);
        return db.insert(DbContract.FilterRules.TABLE_NAME, DbContract.FilterRules.COLUMN_NAME_DESCRIPTION, vals);
    }
}
