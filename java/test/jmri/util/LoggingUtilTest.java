package jmri.util;

import org.junit.jupiter.api.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(JUnitAppender.verifyNoBacklog());        
    }

    @Test
    public void testWarnOnceCounts() {
        assertTrue(LoggingUtil.warnOnce(log, "WARN message")); // string has to be same until further notice
        assertFalse(LoggingUtil.warnOnce(log, "WARN message"));
        JUnitAppender.assertWarnMessage("WARN message");
        assertTrue(JUnitAppender.verifyNoBacklog());

        Logger log2 = LoggerFactory.getLogger("LoggingUtilTest-extra-logger"); // same message, different logger
        assertTrue(LoggingUtil.warnOnce(log2, "WARN message"));
        assertFalse(LoggingUtil.warnOnce(log2, "WARN message"));
        JUnitAppender.assertWarnMessage("WARN message");
        assertTrue(JUnitAppender.verifyNoBacklog());

        assertTrue(LoggingUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        JUnitAppender.assertWarnMessage("WARN message 2");
        assertFalse(LoggingUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceReset() {
        assertTrue(LoggingUtil.warnOnce(log, "WARN message check")); // string has to be same until further notice
        JUnitLoggingUtil.restartWarnOnce();
        assertTrue(LoggingUtil.warnOnce(log, "WARN message check"));
        JUnitAppender.assertWarnMessage("WARN message check");
        JUnitAppender.assertWarnMessage("WARN message check");
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceArguments() {
        assertTrue(LoggingUtil.warnOnce(log, "Test {} {}", "A", "B"));
        JUnitAppender.assertWarnMessage("Test A B");
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testInfoOnceCounts() {
        assertTrue(LoggingUtil.infoOnce(log, "INFO message")); // string has to be same until further notice
        assertFalse(LoggingUtil.infoOnce(log, "INFO message"));
        JUnitAppender.assertMessage("INFO message", Level.INFO );
        assertTrue(JUnitAppender.verifyNoBacklog());

        Logger log2 = LoggerFactory.getLogger("LoggingUtilTest-extra-logger"); // same message, different logger
        assertTrue(LoggingUtil.infoOnce(log2, "INFO message"));
        assertFalse(LoggingUtil.infoOnce(log2, "INFO message"));
        JUnitAppender.assertMessage("INFO message", Level.INFO );
        assertTrue(JUnitAppender.verifyNoBacklog());

        assertTrue(LoggingUtil.infoOnce(log, "INFO message 2")); // same logger, different message
        JUnitAppender.assertMessage("INFO message 2", Level.INFO );
        assertFalse(LoggingUtil.infoOnce(log, "INFO message 2")); // same logger, different message
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

     @Test
    public void testInfoOnceArguments() {
        assertTrue(LoggingUtil.infoOnce(log, "Test {} {}", "A", "B"));
        JUnitAppender.assertMessage("Test A B", Level.INFO );
        assertTrue(JUnitAppender.verifyNoBacklog());
    }
    
    // The following two tests are _identical_.  We run them twice to make
    // sure that two separate tests are properly detecting deprecation messages
    @Test
    public void testDeprecatedWarning1() {

        // on by default
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        assertTrue(JUnitAppender.verifyNoBacklog());

        // logging turned off 
        JUnitLoggingUtil.setDeprecatedLogging(true);
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testDeprecatedWarning2() {

        // on by default
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        assertTrue(JUnitAppender.verifyNoBacklog());

        // logging turned off 
        JUnitLoggingUtil.setDeprecatedLogging(true);
        LoggingUtil.deprecationWarning(log, "method 1");
        LoggingUtil.deprecationWarning(log, "method 1");
        assertTrue(JUnitAppender.verifyNoBacklog());
    }

    private IllegalArgumentException getTraceBack() { return new IllegalArgumentException("for test"); }

    @Test
    public void testShortenStacktrace() {
        IllegalArgumentException ex = getTraceBack();
        assertTrue( ex.getStackTrace().length > 3, "Needs long enough trace for test");

        assertEquals(3, LoggingUtil.shortenStacktrace(ex, 3).getStackTrace().length);
    }

    @Test
    public void testShortenStacktraceTooLong() {
        IllegalArgumentException ex = getTraceBack();
        assertTrue( ex.getStackTrace().length < 3000, "Need short enough trace for test");
        // make sure it doesn't throw an exception
        int len = ex.getStackTrace().length;
        assertEquals(len, LoggingUtil.shortenStacktrace(ex, 3010).getStackTrace().length);
    }

    @Test
    public void testShortenStacktraceNoArg() {
        IllegalArgumentException ex = getTraceBack();
        assertTrue( ex.getStackTrace().length > 3, "Needs long enough trace for test");
        
        assertEquals(2, LoggingUtil.shortenStacktrace(ex).getStackTrace().length);
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
