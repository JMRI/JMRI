package jmri.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(false).isTrue().withFailMessage("This test always fails");
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
