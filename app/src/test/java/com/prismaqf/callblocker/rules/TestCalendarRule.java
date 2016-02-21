package com.prismaqf.callblocker.rules;


import android.os.BaseBundle;
import android.os.Bundle;


import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * @author ConteDiMonteCristo.
 */
public class TestCalendarRule {

    @Test
    public void DefaultConstructorTest() {
        CalendarRule rule = new CalendarRule();
        assertEquals("String representation","Name=always, Days=MTWTFSS, from 00:00 to 23:59",rule.toString());
        assertTrue("Any time is satisfied",rule.IsActive());
    }

    @Test
    public void OnGivenDays() {
        CalendarRule rule = new CalendarRule("first", EnumSet.of(CalendarRule.DayOfWeek.MONDAY,
                                                        CalendarRule.DayOfWeek.THURSDAY));
        //On Monday
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date currentTime = cal.getTime();
        assertTrue("A Monday is ok",rule.IsActive(currentTime));
        //but not on tuesday
        cal.add(Calendar.DAY_OF_YEAR,1);
        currentTime = cal.getTime();
        assertFalse("Tuesday is not ok", rule.IsActive(currentTime));
        assertEquals("String representation", "Name=first, Days=M--T---, from 00:00 to 23:59", rule.toString());
    }

    @Test
    public void OnGiveDayGivenTime() {
        CalendarRule rule = new CalendarRule("first",EnumSet.of(CalendarRule.DayOfWeek.SATURDAY),12,35,16,45);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY,15);
        cal.set(Calendar.MINUTE,25);
        assertTrue("Saturday 15:25 is fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.HOUR_OF_DAY, 9);
        assertFalse("Saturday 9:25 not fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 25);
        assertFalse("Sunday 15:25 not fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 35);
        assertTrue("Saturday 12:35 is fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY,16);
        cal.set(Calendar.MINUTE, 45);
        assertTrue("Saturday 16:45 is fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY,12);
        cal.set(Calendar.MINUTE, 34);
        assertFalse("Saturday 12:34 is not fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY,16);
        cal.set(Calendar.MINUTE, 46);
        assertFalse("Saturday 16:46 is not fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);
        assertFalse("Saturday 00:00 is not fine", rule.IsActive(cal.getTime()));
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY,23);
        cal.set(Calendar.MINUTE, 59);
        assertFalse("Saturday 23:59 is not fine", rule.IsActive(cal.getTime()));
    }

    @Test
    public void testIntMaskToEnumSet() {
        int mask = 86; //should be -TW-F-S
        EnumSet<CalendarRule.DayOfWeek> set = CalendarRule.makeMask(mask);
        assertFalse("Monday is not in set", set.contains(CalendarRule.DayOfWeek.MONDAY));
        assertTrue("Tuesday is in set", set.contains(CalendarRule.DayOfWeek.TUESDAY));
        assertTrue("Wednesday is in set", set.contains(CalendarRule.DayOfWeek.WEDNESDAY));
        assertFalse("Thursday is not in set", set.contains(CalendarRule.DayOfWeek.THURSDAY));
        assertTrue("Friday is in set", set.contains(CalendarRule.DayOfWeek.FRIDAY));
        assertFalse("Saturday is not in set", set.contains(CalendarRule.DayOfWeek.SATURDAY));
        assertTrue("Sunday is in set", set.contains(CalendarRule.DayOfWeek.SUNDAY));
    }

    @Test
    public void testMakeRuleFromBundle() {
        Bundle b = Mockito.mock(Bundle.class);
        when(b.getString(CalendarRule.KEY_NAME)).thenReturn("dummy");
        when(b.getInt(CalendarRule.KEY_DAY_MASK,0)).thenReturn(86);
        when(b.getInt(CalendarRule.KEY_START_HOUR,0)).thenReturn(5);
        when(b.getInt(CalendarRule.KEY_START_MIN,0)).thenReturn(25);
        when(b.getInt(CalendarRule.KEY_END_HOUR,23)).thenReturn(21);
        when(b.getInt(CalendarRule.KEY_END_MIN,59)).thenReturn(7);

        CalendarRule rule = CalendarRule.makeRule(b);
        assertEquals("Rule summary","Name=dummy, Days=-TW-F-S, from 05:25 to 21:07",rule.toString());
    }
}
