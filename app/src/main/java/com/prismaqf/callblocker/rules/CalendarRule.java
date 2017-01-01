package com.prismaqf.callblocker.rules;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

/**
 * Custom calendar rule, with days of week and start stop times
 * @author ConteDiMonteCristo.
 */
public class CalendarRule implements ICalendarRule, Cloneable, Parcelable, Serializable{

    private final static long serialVersionUID = 1L; //for serialization consistency
    private String name;
    private EnumSet<DayOfWeek> dayMask;
    private int startHour;
    private int startMin;
    private int endHour;
    private int endMin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getStartTime() { return String.format(Locale.getDefault(),"From %02d:%02d",startHour, startMin);}

    public String getBareStartTime() { return String.format(Locale.getDefault(),"%02d:%02d",startHour, startMin);}

    public String getEndTime() { return String.format(Locale.getDefault(),"To %02d:%02d",endHour, endMin);}

    public String getBareEndTime() { return String.format(Locale.getDefault(),"%02d:%02d",endHour, endMin);}

    /**
     * A calendar rule based on a mask for the days of the week when the rule should be active
     * and a start stop time given in hour,minute
     * @param name the rule name
     * @param dayMask a set of days of the week for which this rule applies
     * @param startHour the starting hour [0-23]
     * @param startMin the starting minute [0-59]
     * @param endHour the ending hour [0-23]
     * @param endMin the ending minute [0-59]
     */
    public CalendarRule(String name, EnumSet<DayOfWeek> dayMask, int startHour, int startMin, int endHour, int endMin) {
        this.name = name;
        this.dayMask = dayMask;
        this.startHour = startHour;
        this.startMin = startMin;
        this.endHour = endHour;
        this.endMin = endMin;
    }

    /**
     A calendar rule based on a mask for the days of the week when the rule should be active
     * with no filtering on start and stop time
     * @param name the rule name
     * @param dayMask a set of days of the week for which this rule applies
     */
    public CalendarRule(String name, EnumSet<DayOfWeek> dayMask) {
        this.name = name;
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
        name = "always";
        dayMask = EnumSet.allOf(DayOfWeek.class);
        this.startHour = 0;
        this.startMin = 0;
        this.endHour = 23;
        this.endMin = 59;
    }


    @SuppressWarnings("unchecked")
    private CalendarRule(Parcel in) {
        name = in.readString();
        dayMask = (EnumSet<DayOfWeek>)in.readSerializable();
        startHour = in.readInt();
        startMin = in.readInt();
        endHour = in.readInt();
        endMin = in.readInt();
    }

    public static CalendarRule makeRule(String name, int dayMask, String from, String to) {
        CalendarRule rule = new CalendarRule();
        rule.setName(name);
        rule.setDayMask(makeMask(dayMask));
        String[] start = from.split(":");
        if (start.length==2) {
            rule.setStartHour(Integer.valueOf(start[0]));
            rule.setStartMin(Integer.valueOf(start[1]));
        }
        String[] end = to.split(":");
        if (end.length==2) {
            rule.setEndHour(Integer.valueOf(end[0]));
            rule.setEndMin(Integer.valueOf(end[1]));
        }
        return rule;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeSerializable(dayMask);
        dest.writeInt(startHour);
        dest.writeInt(startMin);
        dest.writeInt(endHour);
        dest.writeInt(endMin);
    }

    public static final Parcelable.Creator<CalendarRule> CREATOR
            = new Parcelable.Creator<CalendarRule>() {
        public CalendarRule createFromParcel(Parcel in) {
            return new CalendarRule(in);
        }

        public CalendarRule[] newArray(int size) {
            return new CalendarRule[size];
        }
    };


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

        private final int value;
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
        StringBuilder buffer = new StringBuilder("Name = ");
        buffer.append(name);
        buffer.append(" [Days=");
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
        buffer.append(String.format(Locale.getDefault(),", from %02d:%02d to %02d:%02d]",startHour,startMin,endHour,endMin));
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

    public int getBinaryMask() {
        int bm = 0;
        if (dayMask.contains(DayOfWeek.MONDAY)) bm+=1;
        if (dayMask.contains(DayOfWeek.TUESDAY)) bm+=2;
        if (dayMask.contains(DayOfWeek.WEDNESDAY)) bm+=4;
        if (dayMask.contains(DayOfWeek.THURSDAY)) bm+=8;
        if (dayMask.contains(DayOfWeek.FRIDAY)) bm+=16;
        if (dayMask.contains(DayOfWeek.SATURDAY)) bm+=32;
        if (dayMask.contains(DayOfWeek.SUNDAY)) bm+=64;
        return bm;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CalendarRule)) return false;
        CalendarRule other = (CalendarRule)o;
        return  name.equals(other.name) &&
                dayMask.equals(other.dayMask) &&
                startHour == other.startHour &&
                startMin == other.startMin &&
                endHour == other.endHour &&
                endMin == other.endMin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + dayMask.hashCode();
        result = prime * result + startHour;
        result = prime * result + startMin;
        result = prime * result + endHour;
        result = prime * result + endMin;
        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        CalendarRule other = (CalendarRule) super.clone();
        other.setDayMask(EnumSet.copyOf(getDayMask()));
        return other;
    }
}
