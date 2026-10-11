package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ProgrammingMode class
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class ProgrammingModeTest {

    @Test
    public void testStateCtors() {
        // tests that statics exist, are not equal
        assertTrue(ProgrammingMode.PAGEMODE.equals(ProgrammingMode.PAGEMODE));
        assertTrue(!ProgrammingMode.REGISTERMODE.equals(ProgrammingMode.PAGEMODE));
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
