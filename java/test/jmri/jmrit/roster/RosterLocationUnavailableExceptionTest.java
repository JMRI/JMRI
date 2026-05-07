package jmri.jmrit.roster;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for RosterLocationUnavailableException class.
 *
 * @author Chad Francis (C) 2026
 **/
public class RosterLocationUnavailableExceptionTest {

    @Test
    public void testCtor() {
        Assertions.assertNotNull(
                new RosterLocationUnavailableException("test exception", null, "/some/path/", null),
                "RosterLocationUnavailableException constructor");
    }

    @Test
    public void testGetUnavailablePath() {
        RosterLocationUnavailableException ex = new RosterLocationUnavailableException(
                "test exception", null, "/some/path/", null);
        Assertions.assertEquals("/some/path/", ex.getUnavailablePath());
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
