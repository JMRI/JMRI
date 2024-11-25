package jmri.util.junit.rules;

import jmri.util.*;
import org.junit.*;

/**
 * Test the RetryRule.
 * <p>Note RetryRule only works with JUnit4 Tests.
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
        countPassOnThirdRetry++;
        if ( countPassOnThirdRetry < 4 ) {
            Assert.fail("fail test plus first two retries, will pass on 3rd");
        }
    }

    private static int countPassOnThirdRetry = 0;

    @Test
    public void testJemmyTimeout() {
        countJemmyTimeout++;
        if ( countJemmyTimeout < 4 ) {
            throw new org.netbeans.jemmy.TimeoutExpiredException("fail test plus first two retries, will pass on 3rd");
        }
    }

    private static int countJemmyTimeout = 0;

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
    static public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void afterTest() {
        // tests are run in either order, so cannot assert the warn, just suppress.
        JUnitAppender.suppressWarnMessageStartsWith("testPassOnThirdRetry(jmri.util.junit.rules.RetryRuleTest) : run");
        JUnitAppender.suppressWarnMessageStartsWith("testJemmyTimeout(jmri.util.junit.rules.RetryRuleTest) : run ");
        
    }

    // after entire class as retryrule messes with before/after
    @AfterClass
    static public void tearDown() {
        Assert.assertEquals("countPassOnThirdRetry incremented 4 times",4, countPassOnThirdRetry);
        Assert.assertEquals("countJemmyTimeout incremented 4 times",4, countJemmyTimeout);
        JUnitUtil.tearDown();
    }

}
