package jmri.util;

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
