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

    public void testHandlingLeadingZeros() {
        Assert.assertEquals("01 == 1", 0, ac.compare("01", "1") );
        Assert.assertEquals("1 == 01", 0, ac.compare("1", "01") );
        Assert.assertEquals("001 == 1", 0, ac.compare("001", "1") );
        Assert.assertEquals("1 == 001", 0, ac.compare("1", "001") );
        Assert.assertEquals("0001 == 1", 0, ac.compare("0001", "1") );
        Assert.assertEquals("1 == 0001", 0, ac.compare("1", "0001") );

        Assert.assertEquals("001 == 01", 0, ac.compare("001", "01") );
        Assert.assertEquals("01 == 001", 0, ac.compare("01", "001") );
        
        Assert.assertEquals("00A == 0A", 0, ac.compare("00A", "0A") );
        Assert.assertEquals("0A == 00A", 0, ac.compare("0A", "00A") );

        Assert.assertEquals("B00A == B0A", 0, ac.compare("B00A", "B0A") );
        Assert.assertEquals("B0A == B00A", 0, ac.compare("B0A", "B00A") );

        Assert.assertEquals("100 > 10", +1, ac.compare("100", "10") );
        Assert.assertEquals("10 < 100", -1, ac.compare("10", "100") );

        Assert.assertEquals("00 == 0", 0, ac.compare("00", "0") );
        Assert.assertEquals("0 == 00", 0, ac.compare("0", "00") );
        
        Assert.assertEquals("0 < A", -1, ac.compare("0", "A") );
        Assert.assertEquals("A < 0", +1, ac.compare("A", "0") );

    }
    
    public void testAlphanumCompare1LTA() {
        Assert.assertTrue("1 < A", ac.compare("1", "A") < 0);
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
        
        Assert.assertEquals(" 1.1.10 > 1.1.2", 1, ac.compare("1.1.10", "1.1.2"));
        
    }

    public void testChunkWithLeadingZeros() { // skip leading zero
        Assert.assertEquals("same IS001 IS1", 0, ac.compare("IS001", "IS1"));
    }
    
    public void testAlphanumCompareTestNeedForDots() {
        Assert.assertEquals(" 10.1.1 > 2.1.1", 1, ac.compare("10.1.1", "2.1.1"));
        Assert.assertEquals(" 2.1.1 < 10.1.1", -1, ac.compare("2.1.1", "10.1.1"));
        
        Assert.assertEquals(" 1.10.1 > 1.2.1", 1, ac.compare("1.10.1", "1.2.1"));
        Assert.assertEquals(" 1.2.1 < 1.10.1", -1, ac.compare("1.2.1", "1.10.1"));
        
        Assert.assertEquals(" 1.1.10 > 1.1.2", 1, ac.compare("1.1.10", "1.1.2"));
        Assert.assertEquals(" 1.1.2 < 1.1.10", -1, ac.compare("1.1.2", "1.1.10"));
   }

    public void testHexadecimal() {
        Assert.assertEquals(" A0 < B0", -1, ac.compare("A0", "B0"));

        Assert.assertEquals(" 21.A0 < 21.B0", -1, ac.compare("21.A0", "21.B0"));

        // non-intuitive, but what it does
        Assert.assertEquals(" 21.1F < 21.10", -1, ac.compare("21.1F", "21.10"));
        Assert.assertEquals(" 1F < 10", -1, ac.compare("1F", "10"));
    }

    public void testAlphanumCompareALTB() {
        Assert.assertTrue("A < B", ac.compare("A", "B") < 0);
    }

    public void testAlphanumCompareBGTA() {
        Assert.assertTrue("B > A", ac.compare("B", "A") > 0);
    }

    public void testAlphanumCompareA1LTB1() {
        Assert.assertTrue("A1 < B1", ac.compare("A1", "B1") < 0);
    }

    public void testAlphanumCompareB1GTA1() {
        Assert.assertTrue("A1 > B1", ac.compare("B1", "A1") > 0);
    }

    public void testAlphanumCompareA10LTB1() {
        Assert.assertTrue("A10 < B1", ac.compare("A10", "B1") < 0);
    }

    public void testAlphanumCompareB1GTA10() {
        Assert.assertTrue("B1 > A10", ac.compare("B1", "A10") > 0);
    }

    public void testAlphanumCompareA2LTA10() {
        Assert.assertTrue("A2 < A10", ac.compare("A2", "A10") < 0);
    }

    public void testAlphanumCompareA10GTA2() {
        Assert.assertTrue("A10 > A2", ac.compare("A10", "A2") > 0);
    }

    public void testAlphanumCompareA10LTA010() { // skip leading zero
        Assert.assertTrue("A10 == A010", ac.compare("A10", "A010") == 0);
    }

    public void testAlphanumCompareA010GTA10() { // skip leading zero
        Assert.assertTrue("A010 == A10", ac.compare("A010", "A10") == 0);
    }

    public void testAlphanumCompareA10Z2LTA10Z10() {
        Assert.assertTrue("A10Z2 < A10Z10", ac.compare("A10Z2", "A10Z10") < 0);
    }

    public void testAlphanumCompareA10Z10GTA10Z2() {
        Assert.assertTrue("A10Z10 > A10Z2", ac.compare("A10Z10", "A10Z2") > 0);
    }

    public void testMixedComparison() {     
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
