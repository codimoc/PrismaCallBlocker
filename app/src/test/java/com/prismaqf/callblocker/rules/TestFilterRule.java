package com.prismaqf.callblocker.rules;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author ConteDiMonteCristo.
 */
@RunWith(JUnit4.class)
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
        Pattern p = FilterRule.makeRegex(FilterRule.filterUnwanted(number));
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
        Pattern p = FilterRule.makeRegex(FilterRule.filterUnwanted(number));
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

    @Test
    public void TestEquality() {
        FilterRule fr1 = new FilterRule("first","The first rule");
        FilterRule fr2 = new FilterRule("first","The first rule");
        assertEquals("The two rules are equal",fr1,fr2);
        assertEquals("The hash code are the same",fr1.hashCode(),fr2.hashCode());
        fr2.setDescription("This is actually the second");
        assertFalse("The two rules are different", fr1.equals(fr2));
        assertFalse("The hash code are different", fr1.hashCode() == fr2.hashCode());
        fr2.setDescription(fr1.getDescription());
        fr2.setName("second");
        assertFalse("The two rules are different", fr1.equals(fr2));
        assertFalse("The hash code are different", fr1.hashCode() == fr2.hashCode());
        fr2.setName(fr1.getName());
        assertEquals("The two rules are equal", fr1, fr2);
        assertEquals("The hash code are the same", fr1.hashCode(), fr2.hashCode());
        fr1.addPattern("123");
        assertFalse("The two rules are different (fr1+Pattern)", fr1.equals(fr2));
        assertFalse("The hash code are different (fr1+Pattern)", fr1.hashCode() == fr2.hashCode());
        fr2.addPattern("456");
        assertFalse("The two rules are different ( diff. Pattern)", fr1.equals(fr2));
        assertFalse("The hash code are different (diff. Pattern)", fr1.hashCode() == fr2.hashCode());
        fr2.removePattern("456");
        fr2.addPattern("123");
        assertEquals("The two rules are equal (1 Pattern)", fr1, fr2);
        assertEquals("The hash code are the same (1 Pattern)", fr1.hashCode(), fr2.hashCode());
        fr1.addPattern("456");
        fr2.addPattern("456");
        assertEquals("The two rules are equal (2 Pattern)", fr1, fr2);
        assertEquals("The hash code are the same (2 Pattern)", fr1.hashCode(), fr2.hashCode());
        fr2.removePattern("456");
        fr2.addPattern("4*56");
        assertFalse("The two rules are different ( 2nd Pattern diff.)", fr1.equals(fr2));
        assertFalse("The hash code are different ( 2nd. Pattern diff)", fr1.hashCode() == fr2.hashCode());
    }

    @Test
    public void TestSetInclusion() {
        FilterRule fr1 = new FilterRule("first","The first rule");
        Set<FilterRule> theSet = new HashSet<>();
        theSet.add(fr1);
        assertTrue("Test set inclusion",theSet.contains(fr1));
    }

    @Test
    public void TestCloning() throws CloneNotSupportedException {
        FilterRule fr1 = new FilterRule("first","The first rule");
        FilterRule fr2 = (FilterRule)fr1.clone();
        assertEquals("Cloning works properly",fr1,fr2);
        assertFalse("Equality is not identity",fr1==fr2);
    }
}
