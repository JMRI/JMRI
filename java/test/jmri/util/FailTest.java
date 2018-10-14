package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Test that always fails
 *
 * Do not put this in any package-level test suite. Run this class to test how
 * your testing infrastructure (e.g. CI engine) handles failing tests.
 *
 * @author Bob Jacobsen Copyright 2015
 */
public class FailTest {

    @Test
    public void testAlwaysFails() {
        Assert.fail("This test always fails");
    }

    @Test
    public void testErrorMessage() {
        LoggerFactory.getLogger(FailTest.class).error("This message should cause a failure");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
