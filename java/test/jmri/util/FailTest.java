package jmri.util;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

/**
 * Test that always fails
 *
 * Do not put this in any package-level test suite. Run this class to test how
 * your testing infrastructure (e.g. CI engine) handles failing tests.
 *
 * @author Bob Jacobsen Copyright 2015
 */
@Disabled("Tests test failure, should never be a part of a normal test suite")
public class FailTest {

    @Test
    public void testAlwaysFails() {
        Assertions.assertFalse(true, "This test always fails");
    }

    @Test
    public void testErrorMessage() {
        LoggerFactory.getLogger(FailTest.class).error("This message should cause a failure");
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
