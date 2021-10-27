package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the apps.util.Log4JUtil class.
 *
 * @author Bob Jacobsen Copyright 2003, 2009, 2010, 2015
 */
public class Log4JUtilTest {

    @Test
    public void testLog4JWarnMessage() {
        log.warn("WARN message succeeds");
        JUnitAppender.assertWarnMessage("WARN message succeeds");

        log.debug("DEBUG message"); // should be suppressed see tests.lcf

        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testSendJavaUtilLogInfoMessage() {
        // test that java.util.logging is getting to Log4J
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Log4JUtilTest.class.getName());
        logger.log(java.util.logging.Level.WARNING, "j.u.l WARNING message");
        JUnitAppender.assertWarnMessage("j.u.l WARNING message");

        logger.log(java.util.logging.Level.FINER, "j.u.l FINER message"); // should be suppressed see tests.lcf

        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceCounts() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message")); // string has to be same until further notice
        Assert.assertFalse(Log4JUtil.warnOnce(log, "WARN message"));
        JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        Logger log2 = LoggerFactory.getLogger("Log4JUtilTest-extra-logger"); // same message, different logger
        Assert.assertTrue(Log4JUtil.warnOnce(log2, "WARN message"));
        Assert.assertFalse(Log4JUtil.warnOnce(log2, "WARN message"));
        JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        JUnitAppender.assertWarnMessage("WARN message 2");
        Assert.assertFalse(Log4JUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
   }

    @Test
    public void testWarnOnceReset() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message check")); // string has to be same until further notice
        JUnitLoggingUtil.restartWarnOnce();
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message check"));
        JUnitAppender.assertWarnMessage("WARN message check");
        JUnitAppender.assertWarnMessage("WARN message check");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceArguments() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "Test {} {}", "A", "B"));
        JUnitAppender.assertWarnMessage("Test A B");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    // The following two tests are _identical_.  We run them twice to make
    // sure that two separate tests are properly detecting deprecation messages
    @Test
    public void testDeprecatedWarning1() {

        // on by default
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        // logging turned off
        JUnitLoggingUtil.setDeprecatedLogging(true);
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }
    @Test
    public void testDeprecatedWarning2() {

        // on by default
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        // logging turned off
        JUnitLoggingUtil.setDeprecatedLogging(true);
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    private IllegalArgumentException getTraceBack() { return new IllegalArgumentException("for test"); }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Log4JUtilTest.class);

}
