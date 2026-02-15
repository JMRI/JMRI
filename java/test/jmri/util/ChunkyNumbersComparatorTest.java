package jmri.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the jmri.util.ChunkyNumbersComparator class.
 *
 * @author Bob Jacobsen copyright 2026
 */
public class ChunkyNumbersComparatorTest {

    protected ChunkyNumbersComparator ac;

    // tests are final to make sure they're not overloaded (hence ignored) in any subclass
        
    @Test
    final public void testAlphanumThenNumeric() {
        assertEquals(  0, ac.compare("ATSF 123", "ATSF 123"), "ATSF 123 = ATSF 123");
        assertEquals( -2, ac.compare("ATSF 123", "CN 123"),   "ATSF 123 < CN 123");
        assertEquals( -1, ac.compare("ATSF 123", "ATSF 456"), "ATSF 123 < ATSF 456");
        assertEquals( -1, ac.compare("ATSF 45",  "ATSF 456"), "ATSF 45  < ATSF 456");
        assertEquals( -1, ac.compare("ATSF 54",  "ATSF 456"), "ATSF 54  < ATSF 456");

        assertEquals( +2, ac.compare("CN 123",   "ATSF 123"), "CN 123   > ATSF 123");
        assertEquals( +1, ac.compare("ATSF 456", "ATSF 123"), "ATSF 456 > ATSF 123");
        assertEquals( +1, ac.compare("ATSF 456", "ATSF 45"),  "ATSF 456 > ATSF 45");
        assertEquals( +1, ac.compare("ATSF 456", "ATSF 54"),  "ATSF 456 > ATSF 54");

        assertEquals(  0, ac.compare("A 1 B 2 ATSF 123", "A 1 B 2 ATSF 123"), "A 1 B 2 ATSF 123 = A 1 B 2 ATSF 123");
        assertEquals( -2, ac.compare("A 1 B 2 ATSF 123", "A 1 B 2 CN 123"),   "A 1 B 2 ATSF 123 < A 1 B 2 CN 123");
        assertEquals( -1, ac.compare("A 1 B 2 ATSF 123", "A 1 B 2 ATSF 456"), "A 1 B 2 ATSF 123 < A 1 B 2 ATSF 456");
        assertEquals( -1, ac.compare("A 1 B 2 ATSF 45",  "A 1 B 2 ATSF 456"), "A 1 B 2 ATSF 45  < A 1 B 2 ATSF 456");
        assertEquals( -1, ac.compare("A 1 B 2 ATSF 54",  "A 1 B 2 ATSF 456"), "A 1 B 2 ATSF 54  < A 1 B 2 ATSF 456");
        assertEquals( +2, ac.compare("A 1 B 2 CN 123",   "A 1 B 2 ATSF 123"), "A 1 B 2 CN 123   > A 1 B 2 ATSF 123");
        assertEquals( +1, ac.compare("A 1 B 2 ATSF 456", "A 1 B 2 ATSF 123"), "A 1 B 2 ATSF 456 > A 1 B 2 ATSF 123");
        assertEquals( +1, ac.compare("A 1 B 2 ATSF 456", "A 1 B 2 ATSF 45"),  "A 1 B 2 ATSF 456 > A 1 B 2 ATSF 45");
        assertEquals( +1, ac.compare("A 1 B 2 ATSF 456", "A 1 B 2 ATSF 54"),  "A 1 B 2 ATSF 456 > A 1 B 2 ATSF 54");

    }

    

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        ac = new ChunkyNumbersComparator();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
