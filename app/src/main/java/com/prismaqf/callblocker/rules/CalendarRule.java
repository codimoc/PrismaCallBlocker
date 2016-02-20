package com.prismaqf.callblocker.rules;

import android.os.Bundle;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

/**
 * Custom calendar rule, with days of week and start stop times
 * @author ConteDiMonteCristo.
 */
public class CalendarRule implements ICalendarRule{

    public final static String KEY_DAY_MASK = "com.prismaqf.callblocker:daymask";
    public final static String KEY_START_HOUR = "com.prismaqf.callblocker:starthour";
    public final static String KEY_START_MIN = "com.prismaqf.callblocker:startmin";
    public final static String KEY_END_HOUR = "com.prismaqf.callblocker:endhour";
    public final static String KEY_END_MIN = "com.prismaqf.callblocker:endmin";

    private EnumSet<DayOfWeek> dayMask;
    private int startHour;
    private int startMin;
    private int endHour;
    private int endMin;

    public EnumSet<DayOfWeek> getDayMask() {
        return dayMask;
    }

    public void setDayMask(EnumSet<DayOfWeek> dayMask) {
        this.dayMask = dayMask;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMin() {
        return endMin;
    }

    public void setEndMin(int endMin) {
        this.endMin = endMin;
    }

    /**
     * A calendar rule based on a mask for the days of the week when the rule should be active
     * and a start stop time given in hour,minute
     * @param dayMask a set of days of the week for which this rule applies
     * @param startHour the starting hour [0-23]
     * @param startMin the starting minute [0-59]
     * @param endHour the ending hour [0-23]
     * @param endMin the ending minute [0-59]
     */
    public CalendarRule(EnumSet<DayOfWeek> dayMask, int startHour, int startMin, int endHour, int endMin) {
        this.dayMask = dayMask;
        this.startHour = startHour;
        this.startMin = startMin;
        this.endHour = endHour;
        this.endMin = endMin;
    }

    /**
     A calendar rule based on a mask for the days of the week when the rule should be active
     * with no filtering on start and stop time
     * @param dayMask a set of days of the week for which this rule applies
     */
    public CalendarRule(EnumSet<DayOfWeek> dayMask) {
        this.dayMask = dayMask;
        this.startHour = 0;
        this.startMin = 0;
        this.endHour = 23;
        this.endMin = 59;
    }

    /**
     A Default calendar rule: no filtering
     */
    public CalendarRule() {
        dayMask = EnumSet.allOf(DayOfWeek.class);
        this.startHour = 0;
        this.startMin = 0;
        this.endHour = 23;
        this.endMin = 59;
    }

    public static CalendarRule makeRule(Bundle extras) {
        CalendarRule rule = new CalendarRule();
        rule.setDayMask(makeMask(extras.getInt(KEY_DAY_MASK,0)));
        rule.setStartHour(extras.getInt(KEY_START_HOUR,0));
        rule.setStartMin(extras.getInt(KEY_START_MIN,0));
        rule.setEndHour(extras.getInt(KEY_END_HOUR,23));
        rule.setEndMin(extras.getInt(KEY_END_MIN,59));
        return rule;
    }

    /**
         * Binary mask for day of the week
         */
    public enum DayOfWeek {
            NONE(0),
            MONDAY (1),
            TUESDAY(2),
            WEDNESDAY(3),
            THURSDAY(4),
            FRIDAY(5),
            SATURDAY(6),
            SUNDAY(7);

        private int value;
        DayOfWeek(int value) {
            this.value = value;
        }
        int getValue() {
            return value;
        }


        /**
         * Return day of the week based on the Calendar day of the week
         * @param calDow the java Calendar day of the week
         * @return inttere representation
         */
        static DayOfWeek getDayFromCalDay(final int calDow) {
            switch (calDow) {
                case Calendar.MONDAY:
                    return DayOfWeek.MONDAY;
                case Calendar.TUESDAY:
                    return DayOfWeek.TUESDAY;
                case Calendar.WEDNESDAY:
                    return DayOfWeek.WEDNESDAY;
                case Calendar.THURSDAY:
                    return DayOfWeek.THURSDAY;
                case Calendar.FRIDAY:
                    return DayOfWeek.FRIDAY;
                case Calendar.SATURDAY:
                    return DayOfWeek.SATURDAY;
                case Calendar.SUNDAY:
                    return DayOfWeek.SUNDAY;
            }
            return DayOfWeek.NONE;
        }
    }


    @Override
    public boolean IsActive(Date currentTime) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTime(currentTime);
        DayOfWeek dow = DayOfWeek.getDayFromCalDay(cal.get(Calendar.DAY_OF_WEEK));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        int numMins = hour*60 + min;
        int starTotMins = startHour*60 + startMin;
        int endTotMins = endHour*60 + endMin;

        return dayMask.contains(dow) &&
                numMins >= starTotMins &&
                numMins <= endTotMins;
    }

    @Override
    public boolean IsActive() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        return IsActive(cal.getTime());
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("Days=");
        if (dayMask.contains(DayOfWeek.MONDAY)) buffer.append('M');
        else buffer.append('-');
        if (dayMask.contains(DayOfWeek.TUESDAY)) buffer.append('T');
        else buffer.append('-');
        if (dayMask.contains(DayOfWeek.WEDNESDAY)) buffer.append('W');
        else buffer.append('-');
        if (dayMask.contains(DayOfWeek.THURSDAY)) buffer.append('T');
        else buffer.append('-');
        if (dayMask.contains(DayOfWeek.FRIDAY)) buffer.append('F');
        else buffer.append('-');
        if (dayMask.contains(DayOfWeek.SATURDAY)) buffer.append('S');
        else buffer.append('-');
        if (dayMask.contains(DayOfWeek.SUNDAY)) buffer.append('S');
        else buffer.append('-');
        buffer.append(String.format(", from %02d:%02d to %02d:%02d",startHour,startMin,endHour,endMin));
        return buffer.toString();
    }

    public static EnumSet<DayOfWeek> makeMask(int dm) {
        EnumSet<DayOfWeek> mask = EnumSet.noneOf(DayOfWeek.class);
        if ((dm & 1) == 1) mask.add(DayOfWeek.MONDAY);
        if ((dm & 2) == 2) mask.add(DayOfWeek.TUESDAY);
        if ((dm & 4) == 4) mask.add(DayOfWeek.WEDNESDAY);
        if ((dm & 8) == 8) mask.add(DayOfWeek.THURSDAY);
        if ((dm & 16) == 16) mask.add(DayOfWeek.FRIDAY);
        if ((dm & 32) == 32) mask.add(DayOfWeek.SATURDAY);
        if ((dm & 64) == 64) mask.add(DayOfWeek.SUNDAY);
        return mask;
    }
}
