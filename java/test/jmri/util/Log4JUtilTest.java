package jmri.util;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.Log4JUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2009, 2010, 2015
 */
public class Log4JUtilTest {

    @Test
    public void testLog4JWarnMessage() {
        log.warn("WARN message succeeds");
        jmri.util.JUnitAppender.assertWarnMessage("WARN message succeeds");

        log.debug("DEBUG message"); // should be suppressed see tests.lcf

        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());        
    }

    @Test
    public void testWarnOnceCounts() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message")); // string has to be same until further notice
        Assert.assertFalse(Log4JUtil.warnOnce(log, "WARN message"));
        jmri.util.JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
        
        Logger log2 = LoggerFactory.getLogger("Log4JUtilTest-extra-logger"); // same message, different logger
        Assert.assertTrue(Log4JUtil.warnOnce(log2, "WARN message"));
        Assert.assertFalse(Log4JUtil.warnOnce(log2, "WARN message"));
        jmri.util.JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());

        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        jmri.util.JUnitAppender.assertWarnMessage("WARN message 2");        
        Assert.assertFalse(Log4JUtil.warnOnce(log, "WARN message 2")); // same logger, different message
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
   }

    @Test
    public void testWarnOnceReset() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message check")); // string has to be same until further notice
        Log4JUtil.restartWarnOnce();
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message check"));
        jmri.util.JUnitAppender.assertWarnMessage("WARN message check");
        jmri.util.JUnitAppender.assertWarnMessage("WARN message check");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }

    @Test
    public void testWarnOnceArguments() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "Test {} {}", "A", "B"));
        jmri.util.JUnitAppender.assertWarnMessage("Test A B");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }
    
    // The following two tests are _identical_.  We run them twice to make
    // sure that two separate tests are properly detecting deprecation messages
    @Test
    public void testDeprecatedWarning1() {

        // on by default
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        jmri.util.JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());

        // logging turned off 
        Log4JUtil.setDeprecatedLogging(true);
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }
    @Test
    public void testDeprecatedWarning2() {

        // on by default
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        jmri.util.JUnitAppender.assertWarnMessage("method 1 is deprecated, please remove references to it");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());

        // logging turned off 
        Log4JUtil.setDeprecatedLogging(true);
        Log4JUtil.deprecationWarning(log, "method 1");
        Log4JUtil.deprecationWarning(log, "method 1");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }


    @Test
    public void testSendJavaUtilLogInfoMessage() {
        // test that java.util.logging is getting to Log4J
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Log4JUtilTest.class.getName());
        logger.log(java.util.logging.Level.WARNING, "j.u.l WARNING message");
        jmri.util.JUnitAppender.assertWarnMessage("j.u.l WARNING message");

        logger.log(java.util.logging.Level.FINER, "j.u.l FINER message"); // should be suppressed see tests.lcf

        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }
    
    private IllegalArgumentException getTraceBack() { return new IllegalArgumentException("for test"); }
    
    @Test
    public void testShortenStacktrace() {
        IllegalArgumentException ex = getTraceBack();
        Assert.assertTrue("Needs long enough trace for test", ex.getStackTrace().length > 3);
        
        Assert.assertEquals(3, Log4JUtil.shortenStacktrace(ex, 3).getStackTrace().length);
    }

    @Test
    public void testShortenStacktraceTooLong() {
        IllegalArgumentException ex = getTraceBack();
        Assert.assertTrue("Need short enough trace for test", ex.getStackTrace().length < 3000);
        // make sure it doesn't throw an exception
        int len = ex.getStackTrace().length;
        Assert.assertEquals(len, Log4JUtil.shortenStacktrace(ex, 3010).getStackTrace().length);
    }

    @Test
    public void testShortenStacktraceNoArg() {
        IllegalArgumentException ex = getTraceBack();
        Assert.assertTrue("Needs long enough trace for test", ex.getStackTrace().length > 3);
        
        Assert.assertEquals(2, Log4JUtil.shortenStacktrace(ex).getStackTrace().length);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Log4JUtilTest.class);

}
