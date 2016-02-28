package com.prismaqf.callblocker.rules;

import org.junit.Test;

import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author ConteDiMonteCristo.
 */
public class TestFilterRule {

    @Test
    public void TestRegexCompleteNumber() {
        final String number = "123456789";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid", p);
        assertEquals("The regex is the number itself", number, p.pattern());
    }

    @Test
    public void TestMatchCompleteNumber() {
        final String number = "123456789";
        FilterRule fr = new FilterRule("dummy", "A dummy rule");
        fr.addPattern(number);
        assertTrue("The number matches the pattern", fr.Matches(number));
        assertFalse("The pattern shoudl not match 123", fr.Matches("123"));
        assertFalse("The pattern shoudl not match 9812", fr.Matches("9812"));
        assertTrue("The pattern matches a larger string containing the pattern", fr.Matches("0123456789"));
    }

    @Test
    public void TestMatchingNull() {
        final String number = "123456789";
        FilterRule fr = new FilterRule("dummy", "A dummy rule");
        fr.addPattern(number);
        assertFalse("Null should not match", fr.Matches(null));
    }
    @Test
    public void TestRegexNoNumber() {
        final String number = "";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid", p);
        assertEquals("The regex allows any number", "\\d*", p.pattern());
    }

    @Test
    public void TestMatchNoNumber() {
        final String number = "";
        FilterRule fr = new FilterRule("dummy", "A dummy rule");
        fr.addPattern(number);
        //any number should match
        assertTrue("A random number matches the pattern", fr.Matches("41842"));
        assertTrue("No number matches the pattern", fr.Matches(""));
        //an invalid number should also match
        assertTrue("No number matches the pattern", fr.Matches("invalid"));
    }

    @Test
    public void TestRegexInvalidNumber() {
        final String number = "123ab456d78-";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid", p);
        assertEquals("The regex has stripped unwanted chars", "12345678", p.pattern());
    }

    @Test
    public void TestRegexTwoGroups() {
        final String number = "123*456";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid", p);
        assertEquals("The regex has two groups","123\\d+456",p.pattern());
    }

    @Test
    public void TestMatchTwoGroups() {
        final String number = "123*456";
        FilterRule fr = new FilterRule("dummy", "A dummy rule");
        fr.addPattern(number);
        assertFalse("A number matching only one not good", fr.Matches("1234"));
        assertFalse("A number matching two, but without space, is not good", fr.Matches("123456"));
        assertTrue("A number matching both patterns should match", fr.Matches("012374567"));
        assertTrue("A number matching both patterns should match (with extra char)", fr.Matches("0 1237a456 7"));
        assertFalse("A number matching both patterns with extra char but no number in between is no good", fr.Matches("123 456"));
    }

    @Test
    public void TestRegexTwoGroupsExtraSeparators() {
        final String number = "123***456";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid", p);
        assertEquals("The regex has two groups", "123\\d+456", p.pattern());
    }

    @Test
    public void TestRegexTwoGroupsTrimLeft() {
        final String number = "*123*456";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid",p);
        assertEquals("The regex has two groups","123\\d+456",p.pattern());
    }

    @Test
    public void TestRegexTwoGroupsTrimRight() {
        final String number = "123*456**";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid",p);
        assertEquals("The regex has two groups","123\\d+456",p.pattern());
    }

    @Test
    public void TestRegexThreeGroups() {
        final String number = "123*456*78";
        Pattern p = FilterRule.makeRegex(number);
        assertNotNull("The pattern is valid",p);
        assertEquals("The regex has three groups","123\\d+456\\d+78",p.pattern());
    }

    @Test
    public void TestFilterWithTwoPatterns() {
        FilterRule fr = new FilterRule("dummy", "A dummy rule");
        fr.addPattern("123");
        fr.addPattern("74*56");
        assertTrue("Matching the first", fr.Matches("012356"));
        assertTrue("Matching the second", fr.Matches("01274556"));
        assertFalse("Matching none", fr.Matches("4512779"));
    }

    @Test
    public void TestWithNoPatterns() {
        FilterRule fr = new FilterRule("dummy", "A dummy rule");
        assertFalse("No pattern to match", fr.Matches("4512779"));
        assertFalse("No pattern to match", fr.Matches(""));
        assertFalse("No pattern to match", fr.Matches(null));
    }
}
