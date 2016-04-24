package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prismaqf.callblocker.rules.FilterRule;

import java.util.ArrayList;

/**
 * @author ConteDiMonteCristo
 */
public class FilterRuleProvider {
    private static final String TAG = FilterRuleProvider.class.getCanonicalName();


    /**
     * Insert a FilterRule and the associated FilterPattern objects
     * @param db the SQLite connection
     * @param fr the filter rule
     * @return the rule id
     */
    public static synchronized long InsertRow(SQLiteDatabase db, FilterRule fr) {
        //the behaviour should be transactional, i.e. both insertion in filterrules
        //and filterpatterns should happen or everything should be unwound.
        //Hence the try-catch
        long ruleId=-1;
        try {
            ContentValues vals = new ContentValues();
            vals.put(DbContract.FilterRules.COLUMN_NAME_RULENAME,fr.getName());
            if (fr.getDescription()!=null)
                vals.put(DbContract.FilterRules.COLUMN_NAME_DESCRIPTION,fr.getDescription());
            ruleId = db.insert(DbContract.FilterRules.TABLE_NAME,DbContract.FilterRules.COLUMN_NAME_DESCRIPTION,vals);
            for (String regex: fr.getPatternKeys()) {
                vals = new ContentValues();
                vals.put(DbContract.FilterPatterns.COLUMN_NAME_RULEID,ruleId);
                vals.put(DbContract.FilterPatterns.COLUMN_NAME_PATTERN,regex);
                db.insert(DbContract.FilterPatterns.TABLE_NAME,null,vals);
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            String where = DbContract.FilterPatterns.COLUMN_NAME_RULEID + " = ?";
            String[] args = {String.valueOf(ruleId)};
            db.delete(DbContract.FilterPatterns.TABLE_NAME,where,args);
            where = DbContract.FilterRules._ID + " = ?";
            db.delete(DbContract.FilterRules.TABLE_NAME,where,args);
        }

        return ruleId;
    }

    /**
     * Return all filter rules
     * @param db the SQlite connection
     * @return a list of FilterRules
     */
    public static synchronized ArrayList<FilterRule> AllFilterRules(SQLiteDatabase db) {
        ArrayList<FilterRule> rules = new ArrayList<>();
        Cursor c1 = db.query(DbContract.FilterRules.TABLE_NAME, null, null, null, null, null, null, null);
        while (c1.moveToNext()) {
            long ruleid = c1.getLong(c1.getColumnIndexOrThrow(DbContract.FilterRules._ID));
            String name = c1.getString(c1.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_RULENAME));
            String description = c1.getString(c1.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_DESCRIPTION));
            FilterRule fr = new FilterRule(name,description);
            String selection = DbContract.FilterPatterns.COLUMN_NAME_RULEID + " = ?";
            String[] selectionArgs = { String.valueOf(ruleid) };
            Cursor c2 = db.query(DbContract.FilterPatterns.TABLE_NAME,null,selection,selectionArgs,null,null,null,null);
            while (c2.moveToNext()) {
                String regex = c2.getString(c2.getColumnIndexOrThrow(DbContract.FilterPatterns.COLUMN_NAME_PATTERN));
                fr.addPattern(regex);
            }
            c2.close();
            rules.add(fr);
        }
        c1.close();
        return rules;
    }


    /**
     * Return all names of the rule to prevent re-inserting a rule with a given name
     * @param db the SQLite connection
     * @return a list of names
     */
    public static synchronized ArrayList<String> AllRuleNames(SQLiteDatabase db) {
        Cursor c = db.query(DbContract.FilterRules.TABLE_NAME, null, null, null, null, null, null, null);
        ArrayList<String> names = new ArrayList<>();
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_RULENAME));
            names.add(name);
        }
        c.close();
        return names;
    }

    /**
     * Update a filter rule by changing name and description and
     * deleting and reinserting the filter patterns
     * @param db the SQLite connection
     * @param ruleId the filter rule identifier
     * @param fr the filter rule
     */
    public static synchronized void UpdateFilterRule(SQLiteDatabase db, long ruleId, FilterRule fr) {
        ContentValues vals = new ContentValues();
        vals.put(DbContract.FilterRules.COLUMN_NAME_RULENAME,fr.getName());
        vals.put(DbContract.FilterRules.COLUMN_NAME_DESCRIPTION,fr.getDescription());
        String selection = DbContract.FilterRules._ID + " = ?";
        String[] selectionArgs = { String.valueOf(ruleId) };
        db.update(DbContract.FilterRules.TABLE_NAME,vals,selection,selectionArgs);
        //now delete and reinsert the patterns
        selection = DbContract.FilterPatterns.COLUMN_NAME_RULEID + " = ?";
        db.delete(DbContract.FilterPatterns.TABLE_NAME, selection, selectionArgs);
        for (String regex: fr.getPatternKeys()) {
            vals = new ContentValues();
            vals.put(DbContract.FilterPatterns.COLUMN_NAME_RULEID,ruleId);
            vals.put(DbContract.FilterPatterns.COLUMN_NAME_PATTERN,regex);
            db.insert(DbContract.FilterPatterns.TABLE_NAME,null,vals);
        }
    }

    /**
     * Delete a rule by id
     * @param db the SQlite connection
     * @param ruleid the rule id
     */
    public static synchronized void DeleteFilterRule(SQLiteDatabase db, long ruleid) {
        String where = DbContract.FilterRules._ID + " = ?";
        String[] args = {String.valueOf(ruleid)};
        db.delete(DbContract.FilterRules.TABLE_NAME, where, args);
        where = DbContract.FilterPatterns.COLUMN_NAME_RULEID + " = ?";
        db.delete(DbContract.FilterPatterns.TABLE_NAME,where,args);
    }

    /**
     * Delete a rule by name
     * @param db the SQlite connection
     * @param name the rule name
     */
    public static synchronized void DeleteFilterRule(SQLiteDatabase db, String name) {
        //find the rule id
        String where = DbContract.FilterRules.COLUMN_NAME_RULENAME + " = ?";
        String[] args = {name};
        String[] cols = {DbContract.FilterRules._ID};
        Cursor c = db.query(DbContract.FilterRules.TABLE_NAME,cols,where,args,null,null,null,null);
        if (c.getCount()>0) {
            c.moveToFirst();
            long ruleId = c.getLong(c.getColumnIndexOrThrow(DbContract.FilterRules._ID));
            DeleteFilterRule(db, ruleId);
        }
        c.close();
    }

    public static synchronized FilterRule FindFilterRule(SQLiteDatabase db, long ruleid) {
        String selection = DbContract.FilterRules._ID + " = ?";
        String[] selectionArgs = { String.valueOf(ruleid) };
        Cursor c1 = db.query(DbContract.FilterRules.TABLE_NAME,null,selection,selectionArgs,null,null,null,null);
        if (c1.getCount() >0) {
            c1.moveToFirst();
            String name = c1.getString(c1.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_RULENAME));
            String description = c1.getString(c1.getColumnIndexOrThrow(DbContract.FilterRules.COLUMN_NAME_DESCRIPTION));
            FilterRule fr = new FilterRule(name,description);
            selection = DbContract.FilterPatterns.COLUMN_NAME_RULEID + " = ?";
            Cursor c2 = db.query(DbContract.FilterPatterns.TABLE_NAME,null,selection,selectionArgs,null,null,null,null);
            while (c2.moveToNext()) {
                String regex = c2.getString(c2.getColumnIndexOrThrow(DbContract.FilterPatterns.COLUMN_NAME_PATTERN));
                fr.addPattern(regex);
            }
            c1.close();
            c2.close();
            return fr;
        }
        c1.close();
        return null;
    }

    public static synchronized FilterRule FindFilterRule(SQLiteDatabase db, String ruleName) {
        String where = DbContract.FilterRules.COLUMN_NAME_RULENAME + " = ?";
        String[] args = {ruleName};
        String[] cols = {DbContract.FilterRules._ID};
        Cursor c = db.query(DbContract.FilterRules.TABLE_NAME, cols, where, args, null, null, null, null);
        if (c.moveToLast()) {
            long ruleId = c.getLong(c.getColumnIndexOrThrow(DbContract.FilterRules._ID));
            c.close();
            return FindFilterRule(db,ruleId);
        }
        c.close();
        return null;
    }

    public static synchronized long FindFilterRuleId(SQLiteDatabase db, String ruleName) {
        String selection = DbContract.FilterRules.COLUMN_NAME_RULENAME + " = ?";
        String[] selectionArgs = { ruleName };
        Cursor c = db.query(DbContract.FilterRules.TABLE_NAME,null,selection,selectionArgs,null,null,null,null);
        if (c.getCount() >0) {
            c.moveToLast(); //get the last occurrency (should be only one entry
            return c.getLong(c.getColumnIndexOrThrow(DbContract.FilterRules._ID));
        }
        c.close();
        return 0;
    }
}
