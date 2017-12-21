package jmri.util;

import java.util.Comparator;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.util.AlphanumComparator class.
 *
 * @author	Paul Bender Copyright 2016
 */
public class AlphanumComparatorTest extends TestCase {

    private static AlphanumComparator ac;

    public void testAlphanumCompare1LTA() {
        Assert.assertEquals("1 < A", ac.compare("1", "A") < 0, true);
    }

    public void testAlphanumCompareEquals() {
        Assert.assertEquals(" 1 == 1", 0, ac.compare("1", "1"));
        Assert.assertEquals(" 10 == 10", 0, ac.compare("10", "10"));
        Assert.assertEquals(" 100 == 100", 0, ac.compare("100", "100"));
    }

    public void testAlphanumCompareNumbersGreater() {
        Assert.assertEquals(" 1 > 0", 1, ac.compare("1", "0"));
        Assert.assertEquals(" 10 > 2", 1, ac.compare("10", "2"));
        Assert.assertEquals(" 2 > 1", 1, ac.compare("2", "1"));
    }

    public void testAlphanumCompareNumbersLesser() {
        Assert.assertEquals(" 1 < 2", -1, ac.compare("1", "2"));
        Assert.assertEquals(" 1 < 10", -1, ac.compare("1", "10"));
        Assert.assertEquals(" 2 < 10 ", -1, ac.compare("2", "10"));
    }

    public void testAlphanumCompareNestedNumeric() {
        Assert.assertEquals(" 1.1.0 < 2.1.0", -1, ac.compare("1.1.0", "2.1.0"));
        Assert.assertEquals(" 1.1.1 == 1.1.1", 0, ac.compare("1.1.1", "1.1.1"));
        Assert.assertEquals(" 2.1.0 > 1.1.0", 1, ac.compare("2.1.0", "1.1.0"));

        Assert.assertEquals(" 1.10.0 > 1.2.0", 1, ac.compare("1.10.0", "1.2.0"));
        Assert.assertEquals(" 1.2.0 < 1.10.0", -1, ac.compare("1.2.0", "1.10.0"));
        
        // non-intuitive, but what it does
        Assert.assertEquals(" 1.1.10 < 1.1.2", 1, ac.compare("1.1.10", "1.1.2"));
        
    }

    public void testHexadecimal() {
        Assert.assertEquals(" A0 < B0", -1, ac.compare("A0", "B0"));

        Assert.assertEquals(" 21.1F > 21.10", 1, ac.compare("21.1F", "21.10"));
        Assert.assertEquals(" 21.A0 < 21.B0", -1, ac.compare("21.A0", "21.B0"));

        // non-intuitive, but what it does
        Assert.assertEquals(" 1F < 10", -1, ac.compare("1F", "10"));
    }

    public void testAlphanumCompareALTB() {
        Assert.assertEquals("A < B", ac.compare("A", "B") < 0, true);
    }

    public void testAlphanumCompareBGTA() {
        Assert.assertEquals("B > A", ac.compare("B", "A") > 0, true);
    }

    public void testAlphanumCompareA1LTB1() {
        Assert.assertEquals("A1 < B1", ac.compare("A1", "B1") < 0, true);
    }

    public void testAlphanumCompareB1GTA1() {
        Assert.assertEquals("A1 > B1", ac.compare("B1", "A1") > 0, true);
    }

    public void testAlphanumCompareA10LTB1() {
        Assert.assertEquals("A10 < B1", ac.compare("A10", "B1") < 0, true);
    }

    public void testAlphanumCompareB1GTA10() {
        Assert.assertEquals("B1 > A10", ac.compare("B1", "A10") > 0, true);
    }

    public void testAlphanumCompareA2LTA10() {
        Assert.assertEquals("A2 < A10", ac.compare("A2", "A10") < 0, true);
    }

    public void testAlphanumCompareA10GTA2() {
        Assert.assertEquals("A10 > A2", ac.compare("A10", "A2") > 0, true);
    }

    public void testAlphanumCompareA10LTA010() {
        Assert.assertEquals("A10 < A010", ac.compare("A10", "A010") < 0, true);
    }

    public void testAlphanumCompareA010GTA10() {
        Assert.assertEquals("A010 > A10", ac.compare("A010", "A10") > 0, true);
    }

    public void testAlphanumCompareA10Z2LTA10Z10() {
        Assert.assertEquals("A10Z2 < A10Z10", ac.compare("A10Z2", "A10Z10") < 0, true);
    }

    public void testAlphanumCompareA10Z10GTA10Z2() {
        Assert.assertEquals("A10Z10 > A10Z2", ac.compare("A10Z10", "A10Z2") > 0, true);
    }

    public void testMixedComparison() {     
        System.out.println("start");   
        Assert.assertEquals("IS100 < IS100A", -1, ac.compare("IS100", "IS100A"));
        Assert.assertEquals("IS100A > IS100", +1, ac.compare("IS100A", "IS100"));
    }

    
    // from here down is testing infrastructure
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        ac = new AlphanumComparator();
    }

    @After
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

///    public AlphanumComparatorTest(String s) {
///        super(s);
///    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AlphanumComparatorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(AlphanumComparatorTest.class);
        return suite;
    }
}
