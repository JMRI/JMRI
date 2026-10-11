package jmri.util;

import java.util.Comparator;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the jmri.util.PreferNumericComparator class.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class PreferNumericComparatorTest {

    @Test
    public void testCompareNumbersEquals() {
        Comparator<String> c = new PreferNumericComparator();
        assertEquals( 0, c.compare("1", "1"), " 1 == 1");
        assertEquals( 0, c.compare("10", "10"), " 10 == 10");
        assertEquals( 0, c.compare("100", "100"), " 100 == 100");
    }

    @Test
    public void testCompareNumbersGreater() {
        Comparator<String> c = new PreferNumericComparator();
        assertEquals( 1, c.compare("1", "0"), " 1 > 0");
        assertEquals( 1, c.compare("10", "2"), " 10 > 2");
        assertEquals( 1, c.compare("2", "1"), " 2 > 1");
    }

    @Test
    public void testCompareNumbersLesser() {
        Comparator<String> c = new PreferNumericComparator();
        assertEquals( -1, c.compare("1", "2"), " 1 < 2");
        assertEquals( -1, c.compare("1", "10"), " 1 < 10");
        assertEquals( -1, c.compare("2", "10"), " 2 < 10 ");
    }

    @Test
    public void testBigNumber() {  // these should default to lexical compares
        Comparator<String> c = new PreferNumericComparator();
        assertEquals( 0, c.compare("99999999999999", "99999999999999"), " 99999999999999 = 99999999999999");
        assertEquals( -1, c.compare("99999999999998", "99999999999999"), " 99999999999999 < 99999999999998");
    }

    @Test
    public void testCompareNestedNumeric() {
        Comparator<String> c = new PreferNumericComparator();
        assertEquals( -1, c.compare("1.1.0", "2.1.0"), " 1.1.0 < 2.1.0");
        assertEquals( 0, c.compare("1.1.1", "1.1.1"), " 1.1.1 == 1.1.1");
        assertEquals( 1, c.compare("2.1.0", "1.1.0"), " 2.1.0 > 1.1.0");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
