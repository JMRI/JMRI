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
        log.warn("WARN message");
        jmri.util.JUnitAppender.assertWarnMessage("WARN message");

        log.debug("DEBUG message"); // should be suppressed see tests.lcf

        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());        
    }

    @Test
    public void testWarnOnceCounts() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message")); // string has to be same until further notice
        Assert.assertFalse(Log4JUtil.warnOnce(log, "WARN message"));
        jmri.util.JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
        
        Logger log2 = LoggerFactory.getLogger("Log4JUtilTest-extra-logger");
        Assert.assertTrue(Log4JUtil.warnOnce(log2, "WARN message"));
        Assert.assertFalse(Log4JUtil.warnOnce(log2, "WARN message"));
        jmri.util.JUnitAppender.assertWarnMessage("WARN message");
        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());

        Assert.assertTrue(Log4JUtil.warnOnce(log, "WARN message 2")); // different string
        jmri.util.JUnitAppender.assertWarnMessage("WARN message 2");        
    }

    @Test
    public void testWarnOnceArguments() {
        Assert.assertTrue(Log4JUtil.warnOnce(log, "Test {} {}", "A", "B"));
        jmri.util.JUnitAppender.assertWarnMessage("Test A B");
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
