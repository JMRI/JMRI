package jmri.util;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the jmri.util.AlphanumComparator class.
 *
 * @author Paul Bender Copyright 2016
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
