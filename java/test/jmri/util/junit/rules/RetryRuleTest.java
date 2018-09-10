package jmri.util.junit.rules;

import jmri.util.*;
import org.junit.*;

/**
 * Test the RetryRule
 * @author Bob Jacobsen Copyright 2018	
 */
public class RetryRuleTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3); // first, plus three retries
    
    @Test
    public void testFirstOK() {
        // always passes
    }

    @Test
    public void testPassOnThirdRetry() {
        if (countPassOnThirdRetry++ < 3) {
            Assert.fail("fail test plus first two retries, will pass on 3rd");
        }
        // this is the 3rd pass
    }
    int countPassOnThirdRetry = 0;

    @Test
    public void testJemmyTimeout() {
        if (countJemmyTimeout++ < 3) {
            throw new org.netbeans.jemmy.TimeoutExpiredException("fail test plus first two retries, will pass on 3rd");
        }
        // this is the 3rd pass
    }
    int countJemmyTimeout = 0;

    // Don't have a test for handling of failure after all retries,
    // because that's a failure...
    
    @Test
    @Ignore("Ignoring a test is part of testing RetryRule")
    public void testIgnore() {
        // only Ignore saves us
        Assert.fail("always fails");
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
