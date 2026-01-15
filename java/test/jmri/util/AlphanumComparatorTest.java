package jmri.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.util.AlphanumComparator class.
 *
 * @author Paul Bender Copyright 2016
 */
public class AlphanumComparatorTest {

    protected AlphanumComparator ac;

    // tests are final to make sure they're not overloaded (hence ignored) in subclass
    
    @Test
    final public void testHandlingLeadingZeros() {
        assertEquals( 0, ac.compare("01", "1"), "01 == 1");
        assertEquals( 0, ac.compare("1", "01"), "1 == 01");
        assertEquals( 0, ac.compare("001", "1"), "001 == 1");
        assertEquals( 0, ac.compare("1", "001"), "1 == 001");
        assertEquals( 0, ac.compare("0001", "1"), "0001 == 1");
        assertEquals( 0, ac.compare("1", "0001"), "1 == 0001");

        assertEquals( 0, ac.compare("001", "01"), "001 == 01");
        assertEquals( 0, ac.compare("01", "001"), "01 == 001");

        assertEquals( 0, ac.compare("00A", "0A"), "00A == 0A");
        assertEquals( 0, ac.compare("0A", "00A"), "0A == 00A");

        assertEquals( 0, ac.compare("B00A", "B0A"), "B00A == B0A");
        assertEquals( 0, ac.compare("B0A", "B00A"), "B0A == B00A");

        assertEquals( +1, ac.compare("100", "10"), "100 > 10");
        assertEquals( -1, ac.compare("10", "100"), "10 < 100");

        assertEquals( 0, ac.compare("00", "0"), "00 == 0");
        assertEquals( 0, ac.compare("0", "00"), "0 == 00");

        assertEquals( -1, ac.compare("0", "A"), "0 < A");
        assertEquals( +1, ac.compare("A", "0"), "A < 0");

    }
    
    @Test
    final public void testAlphanumCompare1LTA() {
        assertTrue( ac.compare("1", "A") < 0, "1 < A");
    }

    @Test
    final public void testAlphanumCompareEquals() {
        assertEquals( 0, ac.compare("1", "1"), " 1 == 1");
        assertEquals( 0, ac.compare("10", "10"), " 10 == 10");
        assertEquals( 0, ac.compare("100", "100"), " 100 == 100");
    }

    @Test
    final public void testAlphanumCompareNumbersGreater() {
        assertEquals( 1, ac.compare("1", "0"), " 1 > 0");
        assertEquals( 1, ac.compare("10", "2"), " 10 > 2");
        assertEquals( 1, ac.compare("2", "1"), " 2 > 1");
    }

    @Test
    final public void testAlphanumCompareNumbersLesser() {
        assertEquals( -1, ac.compare("1", "2"), " 1 < 2");
        assertEquals( -1, ac.compare("1", "10"), " 1 < 10");
        assertEquals( -1, ac.compare("2", "10"), " 2 < 10 ");
    }

    @Test
    final public void testAlphanumCompareNestedNumeric() {
        assertEquals( -1, ac.compare("1.1.0", "2.1.0"), " 1.1.0 < 2.1.0");
        assertEquals( 0, ac.compare("1.1.1", "1.1.1"), " 1.1.1 == 1.1.1");
        assertEquals( 1, ac.compare("2.1.0", "1.1.0"), " 2.1.0 > 1.1.0");

        assertEquals( 1, ac.compare("1.10.0", "1.2.0"), " 1.10.0 > 1.2.0");
        assertEquals( -1, ac.compare("1.2.0", "1.10.0"), " 1.2.0 < 1.10.0");
 
        assertEquals( 1, ac.compare("1.1.10", "1.1.2"), " 1.1.10 > 1.1.2");

    }

    @Test
    final public void testChunkWithLeadingZeros() { // skip leading zero
        assertEquals( 0, ac.compare("IS001", "IS1"), "same IS001 IS1");
    }

    @Test
    final public void testAlphanumCompareTestNeedForDots() {
        assertEquals( 1, ac.compare("10.1.1", "2.1.1"), " 10.1.1 > 2.1.1");
        assertEquals( -1, ac.compare("2.1.1", "10.1.1"), " 2.1.1 < 10.1.1");
 
        assertEquals( 1, ac.compare("1.10.1", "1.2.1"), " 1.10.1 > 1.2.1");
        assertEquals( -1, ac.compare("1.2.1", "1.10.1"), " 1.2.1 < 1.10.1");
 
        assertEquals( 1, ac.compare("1.1.10", "1.1.2"), " 1.1.10 > 1.1.2");
        assertEquals( -1, ac.compare("1.1.2", "1.1.10"), " 1.1.2 < 1.1.10");
   }

    @Test
    final public void testHexadecimal() {
        assertEquals( -1, ac.compare("A0", "B0"), " A0 < B0");

        assertEquals( -1, ac.compare("21.A0", "21.B0"), " 21.A0 < 21.B0");

        // non-intuitive, but what it does
        assertEquals( -1, ac.compare("21.1F", "21.10"), " 21.1F < 21.10");
        assertEquals( -1, ac.compare("1F", "10"), " 1F < 10");
    }

    @Test
    final public void testAlphanumCompareALTB() {
        assertTrue( ac.compare("A", "B") < 0, "A < B");
    }

    @Test
    final public void testAlphanumCompareBGTA() {
        assertTrue( ac.compare("B", "A") > 0, "B > A");
    }

    @Test
    final public void testAlphanumCompareA1LTB1() {
        assertTrue( ac.compare("A1", "B1") < 0, "A1 < B1");
    }

    @Test
    final public void testAlphanumCompareB1GTA1() {
        assertTrue( ac.compare("B1", "A1") > 0, "A1 > B1");
    }

    @Test
    final public void testAlphanumCompareA10LTB1() {
        assertTrue( ac.compare("A10", "B1") < 0, "A10 < B1");
    }

    @Test
    final public void testAlphanumCompareB1GTA10() {
        assertTrue( ac.compare("B1", "A10") > 0, "B1 > A10");
    }

    @Test
    final public void testAlphanumCompareA2LTA10() {
        assertTrue( ac.compare("A2", "A10") < 0, "A2 < A10");
    }

    @Test
    final public void testAlphanumCompareA10GTA2() {
        assertTrue( ac.compare("A10", "A2") > 0, "A10 > A2");
    }

    @Test
    final public void testAlphanumCompareA10LTA010() { // skip leading zero
        assertTrue( ac.compare("A10", "A010") == 0, "A10 == A010");
    }

    @Test
    final public void testAlphanumCompareA010GTA10() { // skip leading zero
        assertTrue( ac.compare("A010", "A10") == 0, "A010 == A10");
    }

    @Test
    final public void testAlphanumCompareA10Z2LTA10Z10() {
        assertTrue( ac.compare("A10Z2", "A10Z10") < 0, "A10Z2 < A10Z10");
    }

    @Test
    final public void testAlphanumCompareA10Z10GTA10Z2() {
        assertTrue( ac.compare("A10Z10", "A10Z2") > 0, "A10Z10 > A10Z2");
    }

    @Test
    final public void testMixedComparison() {     
        assertEquals( -1, ac.compare("IS100", "IS100A"), "IS100 < IS100A");
        assertEquals( +1, ac.compare("IS100A", "IS100"), "IS100A > IS100");
    }


    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        ac = new AlphanumComparator();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
