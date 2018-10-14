package jmri.util;

import java.util.Comparator;

import org.junit.*;

/**
 * Tests for the jmri.util.StringUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class PreferNumericComparatorTest {

    @Test
    public void testCompareNumbersEquals() {
        Comparator<String> c = new PreferNumericComparator();
        Assert.assertEquals(" 1 == 1", 0, c.compare("1", "1"));
        Assert.assertEquals(" 10 == 10", 0, c.compare("10", "10"));
        Assert.assertEquals(" 100 == 100", 0, c.compare("100", "100"));
    }

    @Test
    public void testCompareNumbersGreater() {
        Comparator<String> c = new PreferNumericComparator();
        Assert.assertEquals(" 1 > 0", 1, c.compare("1", "0"));
        Assert.assertEquals(" 10 > 2", 1, c.compare("10", "2"));
        Assert.assertEquals(" 2 > 1", 1, c.compare("2", "1"));
    }

    @Test
    public void testCompareNumbersLesser() {
        Comparator<String> c = new PreferNumericComparator();
        Assert.assertEquals(" 1 < 2", -1, c.compare("1", "2"));
        Assert.assertEquals(" 1 < 10", -1, c.compare("1", "10"));
        Assert.assertEquals(" 2 < 10 ", -1, c.compare("2", "10"));
    }

    @Test
    public void testCompareNestedNumeric() {
        Comparator<String> c = new PreferNumericComparator();
        Assert.assertEquals(" 1.1.0 < 2.1.0", -1, c.compare("1.1.0", "2.1.0"));
        Assert.assertEquals(" 1.1.1 == 1.1.1", 0, c.compare("1.1.1", "1.1.1"));
        Assert.assertEquals(" 2.1.0 > 1.1.0", 1, c.compare("2.1.0", "1.1.0"));
    }

}
