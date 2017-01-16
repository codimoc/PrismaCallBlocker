package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prismaqf.callblocker.filters.Filter;
import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ConteDiMonteCristo
 */
public class FilterProvider {
    private final static String TAG = FilterProvider.class.getCanonicalName();

    /**
     * Insert a Filter record (a filter handle actually)
     * @param db the SQLite db connection
     * @param fh the handle with the relevant info
     * @return the id of the filter or -1 if there is a problem
     */
    public static synchronized long InsertRow(SQLiteDatabase db, FilterHandle fh) {
        if (fh.getName()==null || fh.getName().isEmpty()) {
            Log.e(TAG,"The filter name is null or empty" );
            return -1;
        }
        ContentValues vals = new ContentValues();
        vals.put(DbContract.Filters.COLUMN_NAME_FILTERNAME,fh.getName());
        if (fh.getCalendarRuleName()!=null)
            vals.put(DbContract.Filters.COLUMN_NAME_CALENDARRULENAME,fh.getCalendarRuleName());
        if (fh.getFilterRuleName() != null)
            vals.put(DbContract.Filters.COLUMN_NAME_FILTERRULENAME,fh.getFilterRuleName());
        if (fh.getActionName() != null)
            vals.put(DbContract.Filters.COLUMN_NAME_ACTIONNAME,fh.getActionName());
        return db.insert(DbContract.Filters.TABLE_NAME, DbContract.Filters.COLUMN_NAME_ACTIONNAME, vals);
    }

    /**
     * Save a Filter from its binary representation
     * @param db the SQLite db connection
     * @param f the filter object
     * @return the id of the filter or -1 if there is a problem
     */
    public static synchronized long SaveFilter(SQLiteDatabase db, Filter f) {
        if (HasFilter(db,f)) return -1L;
        String fname = f.getName();
        if (FindFilter(db,fname) != null)
            fname = String.format("%s_#",fname);

        //Save the content here
        //for calendar rules we try to find an equivalent rule so we don't save
        //if no equivalent we save.
        //In any case we resolve name ambiguity when we save
        CalendarRule cr = (CalendarRule) f.getCalendarRule();
        String cname = f.getCalendarRule().getName();
        if (cr != null) { //ICalendarRule is a CalendarRule
            String oldName = CalendarRuleProvider.FindCalendarRule(db, cr);
            if (oldName == null) //an equivalent rule does not exist
            {
                if (CalendarRuleProvider.FindCalendarRule(db,cname)!=null) //the name is already used
                {
                    cname = String.format("%s_#",cname);
                    cr.setName(cname);
                }
                CalendarRuleProvider.InsertRow(db, cr);
            }
            else
                cname = oldName;
        }
        //for filter rules we always save and
        //we resolve name ambiguity when we save
        FilterRule fr = (FilterRule)f.getFilterRule();
        String frname = f.getFilterRule().getName();
        if (fr != null) { //IFilterRule is a FilterRule
            if (FilterRuleProvider.FindFilterRule(db,frname) != null) //the name is already used
            {
                frname = String.format("%s_#",frname);
                fr.setName(frname);
            }
            FilterRuleProvider.InsertRow(db, fr);
        }
        //for actions instead there is nothing that need to be saved

        //Save the filter handle here
        FilterHandle fh = new FilterHandle(fname,cname,frname,f.getAction().getName());
        return InsertRow(db,fh);
    }

    /**
     * Return all the filters from DB
     * @param db the SQLite db connection
     * @return the entire collection of filters
     */
    public static synchronized ArrayList<FilterHandle> AllFilters(SQLiteDatabase db) {
        ArrayList<FilterHandle> filters = new ArrayList<>();
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, null, null, null, null, null, null, null);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERNAME));
            String calendar = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_CALENDARRULENAME));
            String patterns = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERRULENAME));
            String action = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_ACTIONNAME));
            filters.add(new FilterHandle(name,calendar,patterns,action));
        }
        c.close();
        return filters;
    }

    /**
     * Return all names of the filters to prevent re-inserting a filter with an existing name
     * @param db the SQLite connection
     * @return a list of names
     */
    public static synchronized ArrayList<String> AllFilterNames(SQLiteDatabase db) {
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, null, null, null, null, null, null, null);
        ArrayList<String> names = new ArrayList<>();
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERNAME));
            names.add(name);
        }
        c.close();
        return names;
    }

    /**
     * Update a filter
     * @param db the SQLite connection
     * @param filterId the id of the filter
     * @param fh the filter handle
     */
    public static synchronized void UpdateFilter(SQLiteDatabase db, long filterId, FilterHandle fh) {
        ContentValues vals = new ContentValues();
        if (fh.getCalendarRuleName()!=null)
            vals.put(DbContract.Filters.COLUMN_NAME_CALENDARRULENAME,fh.getCalendarRuleName());
        if (fh.getFilterRuleName() != null)
            vals.put(DbContract.Filters.COLUMN_NAME_FILTERRULENAME,fh.getFilterRuleName());
        if (fh.getActionName() != null)
            vals.put(DbContract.Filters.COLUMN_NAME_ACTIONNAME,fh.getActionName());
        String selection = DbContract.Filters._ID + " = ?";
        String[] selectionArgs = { String.valueOf(filterId) };
        db.update(DbContract.Filters.TABLE_NAME, vals, selection, selectionArgs);
    }

    /**
     * Delete a filter by id
     * @param db the SQlite connection
     * @param filterId the filter id
     */
    public static synchronized void DeleteFilter(SQLiteDatabase db, long filterId) {
        String where = DbContract.Filters._ID + " = ?";
        String[] args = {String.valueOf(filterId)};
        db.delete(DbContract.Filters.TABLE_NAME, where, args);
        where = DbContract.FilterPatterns.COLUMN_NAME_RULEID + " = ?";
        db.delete(DbContract.FilterPatterns.TABLE_NAME, where, args);
    }

    /**
     * Delete a filter by name
     * @param db the SQlite connection
     * @param name the filter name
     */
    public static synchronized void DeleteFilter(SQLiteDatabase db, String name) {
        //find the filter id
        String where = DbContract.Filters.COLUMN_NAME_FILTERNAME + " = ?";
        String[] args = {name};
        String[] cols = {DbContract.Filters._ID};
        Cursor c = db.query(DbContract.Filters.TABLE_NAME,cols,where,args,null,null,null,null);
        if (c.getCount()>0) {
            c.moveToFirst();
            long filterId = c.getLong(c.getColumnIndexOrThrow(DbContract.Filters._ID));
            DeleteFilter(db, filterId);
        }
        c.close();
    }

    /**
     * Retrieve a filter handle by id
     * @param db the SQlite connection
     * @param filterId the filter id
     * @return the handle to the filter
     */
    public static synchronized FilterHandle FindFilter(SQLiteDatabase db, long filterId) {
        String selection = DbContract.Filters._ID + " = ?";
        String[] selectionArgs = { String.valueOf(filterId) };
        Cursor c = db.query(DbContract.Filters.TABLE_NAME,null,selection,selectionArgs,null,null,null,null);
        if (c.moveToFirst()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERNAME));
            String calendar = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_CALENDARRULENAME));
            String patterns = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERRULENAME));
            String action = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_ACTIONNAME));
            c.close();
            return new FilterHandle(name,calendar,patterns,action);
        }
        c.close();
        return null;
    }

    /**
     * Return a filter by name
     * @param db the SQLite connection
     * @param name the filter name
     * @return a handle to the filter
     */
    public static synchronized FilterHandle FindFilter(SQLiteDatabase db, String name) {
        String where = DbContract.Filters.COLUMN_NAME_FILTERNAME + " = ?";
        String[] args = {name};
        String[] cols = {DbContract.Filters._ID};
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, cols, where, args, null, null, null, null);
        if (c.moveToLast()) {
            long filterId = c.getLong(c.getColumnIndexOrThrow(DbContract.Filters._ID));
            c.close();
            return FindFilter(db,filterId);
        }
        c.close();
        return null;
    }

    /**
     * Checks if the given calendar rule name is used in a filter
     * @param db the SQLite connection
     * @param ruleName the rule name
     * @return a flag indicating if the rule exixts or not
     */
    public static synchronized boolean HasCalendarRule(SQLiteDatabase db, String ruleName) {
        String where = DbContract.Filters.COLUMN_NAME_CALENDARRULENAME + " = ?";
        String[] args = {ruleName};
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, null, where, args, null, null, null, null);
        boolean flag = false;
        if (c.getCount() > 0) flag = true;
        c.close();
        return flag;
    }

    /**
     * Checks if the given filter rule name is used in a filter
     * @param db the SQLite connection
     * @param ruleName the rule name
     * @return a flag indicating if the rule exixts or not
     */
    public static synchronized boolean HasFilterRule(SQLiteDatabase db, String ruleName) {
        String where = DbContract.Filters.COLUMN_NAME_FILTERRULENAME + " = ?";
        String[] args = {ruleName};
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, null, where, args, null, null, null, null);
        boolean flag = false;
        if (c.getCount() > 0) flag = true;
        c.close();
        return flag;
    }

    /**
     * Check if a filter with the same rules already exists
     * @param db the SQLite connection
     * @param filter the filter object
     * @return a flag indicating if the rule exixts or not
     */
    public static synchronized boolean HasFilter (SQLiteDatabase db, Filter filter) {
        String where = DbContract.Filters.COLUMN_NAME_FILTERRULENAME + " = ? AND " +
                       DbContract.Filters.COLUMN_NAME_CALENDARRULENAME + " = ? AND " +
                       DbContract.Filters.COLUMN_NAME_ACTIONNAME + " = ?";
        String[] args = {filter.getFilterRule().getName(),
                         filter.getCalendarRule().getName(),
                         filter.getAction().getName()};
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, null, where, args, null, null, null, null);
        boolean flag = false;
        if (c.getCount() > 0) flag = true;
        c.close();
        return flag;
    }

    /**
     * Returns a collection of all existing filter handles
     * @param db the SQLite db connection
     * @return a list of filter handles
     */
    public static synchronized List<FilterHandle> LoadFilters(SQLiteDatabase db) {
        List<FilterHandle> handles = new ArrayList<>();
        Cursor c = db.query(DbContract.Filters.TABLE_NAME, null, null, null, null, null, null, null);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERNAME));
            String calendar = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_CALENDARRULENAME));
            String patterns = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_FILTERRULENAME));
            String action = c.getString(c.getColumnIndexOrThrow(DbContract.Filters.COLUMN_NAME_ACTIONNAME));
            handles.add(new FilterHandle(name,calendar,patterns,action));
        }
        c.close();
        return handles;
    }
}
