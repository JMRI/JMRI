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
    }
    int countPassOnThirdRetry = 0;

    @Test
    public void testJemmyTimeout() {
        if (countJemmyTimeout++ < 3) {
            throw new org.netbeans.jemmy.TimeoutExpiredException("fail test plus first two retries, will pass on 3rd");
        }
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

    // before entire class as retryrule messes with before/after
    @BeforeClass
    // The minimal setup for log4J
    static public void setUp() {
        JUnitUtil.setUp();
    }
    
    @After
    public void afterTest() {
        // test for messages
        if (countPassOnThirdRetry == 4) countPassOnThirdRetry = 0; // done
        if (countPassOnThirdRetry != 0) {
            jmri.util.JUnitAppender.assertWarnMessage("run  "+countPassOnThirdRetry+" failed, RetryRule repeats");
        }

        if (countJemmyTimeout == 4) countJemmyTimeout = 0; // done
        if (countJemmyTimeout != 0) {
            jmri.util.JUnitAppender.assertWarnMessage("run  "+countJemmyTimeout+" failed, RetryRule repeats");
        }
        
        jmri.util.JUnitAppender.suppressWarnMessage("run  3 failed, RetryRule repeats");
    }

    // before entire class as retryrule messes with before/after
    @AfterClass
    static public void tearDown() {
        JUnitUtil.tearDown();
    }

}
