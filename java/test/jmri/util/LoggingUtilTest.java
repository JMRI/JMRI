package jmri.util;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Tests for the jmri.util.LoggingUtil and jmir.util.JUnitLoggingUtil classes.
 *
 * @author Bob Jacobsen Copyright 2003, 2009, 2010, 2015
 */
public class LoggingUtilTest {

    @Test
    public void testLoggingWarnMessage() {
        log.warn("WARN message succeeds");
        JUnitAppender.assertWarnMessage("WARN message succeeds");

        log.debug("DEBUG message"); // should be suppressed see tests_lcf.xml

        Assert.assertTrue(JUnitAppender.verifyNoBacklog());        
    }

    @Test
    public void testWarnOnceCounts() {
        Assert.assertTrue(LoggingUtil.warnOnce(log, "WARN message")); // string has to be same until further notice
        Assert.assertFalse(LoggingUtil.warnOnce(log, "WARN message"));
        JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        Logger log2 = LoggerFactory.getLogger("LoggingUtilTest-extra-logger"); // same message, different logger
        Assert.assertTrue(LoggingUtil.warnOnce(log2, "WARN message"));
        Assert.assertFalse(LoggingUtil.warnOnce(log2, "WARN message"));
        JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        Assert.assertTrue(LoggingUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        JUnitAppender.assertWarnMessage("WARN message 2");
        Assert.assertFalse(LoggingUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceReset() {
        Assert.assertTrue(LoggingUtil.warnOnce(log, "WARN message check")); // string has to be same until further notice
        JUnitLoggingUtil.restartWarnOnce();
        Assert.assertTrue(LoggingUtil.warnOnce(log, "WARN message check"));
        JUnitAppender.assertWarnMessage("WARN message check");
        JUnitAppender.assertWarnMessage("WARN message check");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceArguments() {
        Assert.assertTrue(LoggingUtil.warnOnce(log, "Test {} {}", "A", "B"));
        JUnitAppender.assertWarnMessage("Test A B");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testInfoOnceCounts() {
        Assert.assertTrue(LoggingUtil.infoOnce(log, "INFO message")); // string has to be same until further notice
        Assert.assertFalse(LoggingUtil.infoOnce(log, "INFO message"));
        JUnitAppender.assertMessage("INFO message", Level.INFO );
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        Logger log2 = LoggerFactory.getLogger("LoggingUtilTest-extra-logger"); // same message, different logger
        Assert.assertTrue(LoggingUtil.infoOnce(log2, "INFO message"));
        Assert.assertFalse(LoggingUtil.infoOnce(log2, "INFO message"));
        JUnitAppender.assertMessage("INFO message", Level.INFO );
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        Assert.assertTrue(LoggingUtil.infoOnce(log, "INFO message 2")); // same logger, different message
        JUnitAppender.assertMessage("INFO message 2", Level.INFO );
        Assert.assertFalse(LoggingUtil.infoOnce(log, "INFO message 2")); // same logger, different message
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

     @Test
    public void testInfoOnceArguments() {
        Assert.assertTrue(LoggingUtil.infoOnce(log, "Test {} {}", "A", "B"));
        JUnitAppender.assertMessage("Test A B", Level.INFO );
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }
    
    // The following two tests are _identical_.  We run them twice to make
    // sure that two separate tests are properly detecting deprecation messages
    @Test
    public void testDeprecatedWarning1() {

        // on by default
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        // logging turned off 
        JUnitLoggingUtil.setDeprecatedLogging(true);
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testDeprecatedWarning2() {

        // on by default
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());

        // logging turned off 
        JUnitLoggingUtil.setDeprecatedLogging(true);
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        Assert.assertTrue(JUnitAppender.verifyNoBacklog());
    }

    private IllegalArgumentException getTraceBack() { return new IllegalArgumentException("for test"); }

    @Test
    public void testShortenStacktrace() {
        IllegalArgumentException ex = getTraceBack();
        Assert.assertTrue("Needs long enough trace for test", ex.getStackTrace().length > 3);

        Assert.assertEquals(3, LoggingUtil.shortenStacktrace(ex, 3).getStackTrace().length);
    }

    @Test
    public void testShortenStacktraceTooLong() {
        IllegalArgumentException ex = getTraceBack();
        Assert.assertTrue("Need short enough trace for test", ex.getStackTrace().length < 3000);
        // make sure it doesn't throw an exception
        int len = ex.getStackTrace().length;
        Assert.assertEquals(len, LoggingUtil.shortenStacktrace(ex, 3010).getStackTrace().length);
    }

    @Test
    public void testShortenStacktraceNoArg() {
        IllegalArgumentException ex = getTraceBack();
        Assert.assertTrue("Needs long enough trace for test", ex.getStackTrace().length > 3);
        
        Assert.assertEquals(2, LoggingUtil.shortenStacktrace(ex).getStackTrace().length);
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LoggingUtilTest.class);

}
