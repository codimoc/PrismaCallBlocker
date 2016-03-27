package com.prismaqf.callblocker.filters;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prismaqf.callblocker.actions.ActionRegistry;
import com.prismaqf.callblocker.actions.IAction;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.rules.ICalendarRule;
import com.prismaqf.callblocker.rules.IFilterRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.sql.SQLException;

/**
 * A filter is a combination of a calendar rule, a filter rule
 * and an action, and basically performs an action when the
 * two rules matches
 * @author ConteDiMonteCristo
 */
public class Filter{

    private static final String TAG = Filter.class.getCanonicalName();

    private final ICalendarRule calendarRule;
    private final IFilterRule filterRule;
    private final IAction action;
    private final String name;


    public ICalendarRule getCalendarRule() {
        return calendarRule;
    }

    public IFilterRule getFilterRule() {
        return filterRule;
    }

    public IAction getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    private Filter(String name, ICalendarRule calendarRule, IFilterRule filterRule, IAction action) {
        this.name = name;
        this.calendarRule = calendarRule;
        this.filterRule = filterRule;
        this.action = action;
    }


    /**
     * Create a Filter from a handle
     * @param ctx the Android context
     * @param handle the handle
     * @return a Filter object
     * @throws SQLException data not founf
     * @throws ReflectiveOperationException could not construct an action
     */
    public static Filter makeFilter(Context ctx, FilterHandle handle )
            throws SQLException, ReflectiveOperationException {
        SQLiteDatabase db=null;
        try {
           db  = new DbHelper(ctx).getReadableDatabase();
            //todo: what to do when the names are null? Use default rule and actions
            CalendarRule cr = CalendarRuleProvider.FindCalendarRule(db, handle.getCalendarRuleName());
            if (cr==null) {
                String msg = String.format("Can't find a calendar rule with name %s", handle.getCalendarRuleName());
                Log.e(TAG, msg);
                throw new SQLException(msg);
            }
            FilterRule fr = FilterRuleProvider.FindFilterRule(db,handle.getFilterRuleName());
            if (fr==null) {
                String msg = String.format("Can't find a filter rule with name %s", handle.getFilterRuleName());
                Log.e(TAG, msg);
                throw new SQLException(msg);
            }
            IAction action = ActionRegistry.getAvailableAction(ctx,handle.getActionName());
            if (action==null) {
                String msg = String.format("Can't find an action with class %s", handle.getActionName());
                Log.e(TAG, msg);
                throw new IllegalArgumentException(msg);
            }
            return new Filter(handle.getName(), cr, fr,action);
        }
        finally {
            if (db != null) db.close();
        }
    }

    @Override
    public String toString() {
        return String.format("Filter %s = [calendar=%s, patterns=%s, action=%s]",name,calendarRule,filterRule,action);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Filter)) return false;
        Filter other = (Filter)o;
        if (!name.equals(other.name)) return false;
        if (!calendarRule.equals(other.calendarRule)) return false;
        if (!filterRule.equals(other.filterRule)) return false;
        if (!action.getClass().getCanonicalName().equals(other.action.getClass().getCanonicalName())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + calendarRule.hashCode();
        result = prime * result + filterRule.hashCode();
        result = prime * result + action.getClass().getCanonicalName().hashCode();
        return result;
    }
}
