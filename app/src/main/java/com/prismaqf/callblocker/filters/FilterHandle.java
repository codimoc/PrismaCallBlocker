package com.prismaqf.callblocker.filters;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is a light handle for a Filter object, used to edit the components
 * forming the filter, i.e. the calendar and filter ruls and the action
 * to be performed. The Filter object will be created from this handle only
 * by the running service, hence the handle is a proxy for editing only
 * @author ConteDiMonteCristo
 */
public class FilterHandle implements Cloneable, Parcelable {

    private String name;
    private String calendarRuleName;
    private String filterRuleName;
    private String actionName;

    private FilterHandle(Parcel in) {
        name = in.readString();
        calendarRuleName = in.readString();
        filterRuleName = in.readString();
        actionName = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getCalendarRuleName() {
        return calendarRuleName==null ? "" : calendarRuleName;
    }

    public String getFilterRuleName() {
        return filterRuleName==null ? "" : filterRuleName;
    }

    public String getActionName() {
        return actionName==null ? "" : actionName;
    }

    public String getActionSimpleName() {
        if (actionName==null) return "";
        int lastDot = actionName.lastIndexOf ('.');
        if (lastDot==-1) return actionName;
        return actionName.substring (lastDot+1,actionName.length());
    }

    public FilterHandle(String name, String calendarRuleName, String filterRuleName, String actionName) {
        this.name = name;
        this.calendarRuleName = calendarRuleName;
        this.filterRuleName = filterRuleName;
        this.actionName = actionName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCalendarRuleName(String calendarRuleName) {
        this.calendarRuleName = calendarRuleName;
    }

    public void setFilterRuleName(String filterRuleName) {
        this.filterRuleName = filterRuleName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return String.format("Filter %s = [calendar=%s, patterns=%s, action=%s]", name, calendarRuleName, filterRuleName, actionName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FilterHandle)) return false;
        FilterHandle other = (FilterHandle) o;
        return name.equals(other.name) &&
               calendarRuleName.equals(other.calendarRuleName) &&
               filterRuleName.equals(other.filterRuleName) &&
               actionName.equals(other.actionName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + calendarRuleName.hashCode();
        result = prime * result + filterRuleName.hashCode();
        result = prime * result + actionName.hashCode();
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(calendarRuleName);
        dest.writeString(filterRuleName);
        dest.writeString(actionName);
    }

    public static final Parcelable.Creator<FilterHandle> CREATOR
            = new Parcelable.Creator<FilterHandle>() {
        public FilterHandle createFromParcel(Parcel in) {
            return new FilterHandle(in);
        }

        public FilterHandle[] newArray(int size) {
            return new FilterHandle[size];
        }
    };
}
