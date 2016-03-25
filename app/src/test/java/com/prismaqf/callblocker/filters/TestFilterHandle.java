package com.prismaqf.callblocker.filters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;

@RunWith(JUnit4.class)
public class TestFilterHandle {

    @Test
    public void testEquality() throws CloneNotSupportedException {
        FilterHandle fh1 = new FilterHandle("first", "cal1", "patt1", "act1");
        FilterHandle fh2 = new FilterHandle("first", "cal1", "patt1", "act1");
        assertEquals("Equality of two filter handles", fh1, fh2);
        assertFalse("Not identity", fh1 == fh2);
        assertEquals("Same hash code", fh1.hashCode(),fh2.hashCode());
        //now cloning
        FilterHandle fh3 = (FilterHandle)fh1.clone();
        assertEquals("Equality after cloning", fh1, fh3);
        assertFalse("Not identity after cloning", fh1 == fh3);
        assertEquals("Same hash code after cloning", fh1.hashCode(),fh3.hashCode());
        //different
        FilterHandle fh4 = new FilterHandle("first", "cal1", "patt1", "act2");
        assertFalse("Not equal", fh1.equals(fh4));
        FilterHandle fh5 = new FilterHandle("first", "cal1", "patt2", "act1");
        assertFalse("Not equal", fh1.equals(fh5));
        FilterHandle fh6 = new FilterHandle("first", "cal2", "patt1", "act1");
        assertFalse("Not equal", fh1.equals(fh6));
        FilterHandle fh7 = new FilterHandle("second", "cal1", "patt1", "act1");
        assertFalse("Not equal", fh1.equals(fh7));
    }
}
