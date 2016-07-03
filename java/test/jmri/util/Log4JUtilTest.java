package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.Log4JUtil class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2009, 2010, 2015
 */
public class Log4JUtilTest extends TestCase {

    public void testLog4JWarnMessage() {
        log.warn("WARN message");
        jmri.util.JUnitAppender.assertWarnMessage("WARN message");
        
        log.debug("DEBUG message"); // should be suppressed see tests.lcf

        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }
    
    public void testSendJavaUtilLogInfoMessage() {
        // test that java.util.logging is getting to Log4J
        java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Log4JUtilTest.class.getName());
        logger.log(java.util.logging.Level.WARNING, "j.u.l WARNING message");
        jmri.util.JUnitAppender.assertWarnMessage("j.u.l WARNING message");

        logger.log(java.util.logging.Level.FINER, "j.u.l FINER message"); // should be suppressed see tests.lcf

        Assert.assertTrue(jmri.util.JUnitAppender.verifyNoBacklog());
    }
    
    // from here down is testing infrastructure
    public Log4JUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Log4JUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Log4JUtilTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Log4JUtilTest.class.getName());

}
