package jmri.jmrit.symbolicprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.jmrit.symbolicprog.CVNameComparator class.
 *
 * @author Paul Bender Copyright 2016
 */
public class CVNameComparatorTest extends jmri.util.AlphanumComparatorTest {

    // ac is the object under test in superclass 
    @Test
    public void testDotOrder() {

        assertTrue( ac.compare("2", "1.1") < 0, "2 < 1.1");
        assertTrue( ac.compare("1.1", "2") > 0, "1.1 > 2");

        assertTrue( ac.compare("1.2", "1.1.1") < 0, "1.2 < 1.1.1");
        assertTrue( ac.compare("1.1.1", "1.2") > 0, "1.1.2 > 1.2");

        // odd cases
        assertTrue( ac.compare("2.", "1.1") > 0, "2. > 1.1");
        assertTrue( ac.compare("1.1", "2.") < 0, "1.1 < 2.");

        assertTrue( ac.compare("2.", "1..1") > 0, "2. > 1..1");
        assertTrue( ac.compare("1..1", "2.") < 0, "1..1 < 2.");

        assertTrue( ac.compare("1.1.1", "1..1") > 0, "1.1.1 > 1..1");
        assertTrue( ac.compare("1..1", "1.1.1") < 0, "1..1 < 1.1.1");

        assertTrue( ac.compare(".2.", "1.1") < 0, ".2. < 1.1");
        assertTrue( ac.compare("1.1", ".2.") > 0, "1.1 > .2.");
    }

    
    // from here down is testing infrastructure
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        ac = new CVNameComparator();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();

    }

}
