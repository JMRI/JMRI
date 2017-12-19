package jmri.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Level;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.JUnitAppender class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class JUnitAppenderTest extends TestCase {

    /**
     * If this constant is true, some tests will run that are expected to log
     * output; this output has to be checked by hand.
     */
    boolean allTests = false;

    public void testInstance() {
        Assert.assertTrue("Instance exists, e.g. initialization for tests OK", JUnitAppender.instance() != null);
    }

    public void testExpectedErrorMessage() {
        String msg = "Message for testing";
        log.error(msg);
        JUnitAppender.assertErrorMessage(msg);
    }

    public void testCheckForMessageError() {
        String msg = "Message for testing to find";
        log.error("Dummy");
        Assert.assertNull(JUnitAppender.checkForMessage(msg));
        log.error(msg);
        Assert.assertNotNull(JUnitAppender.checkForMessage(msg));
        // second not match
        Assert.assertNull(JUnitAppender.checkForMessage(msg));
    }
    
    public void testCheckForMessageWarn() {
        String msg = "Message for testing to find";
        log.error("Dummy");
        Assert.assertNull(JUnitAppender.checkForMessage(msg));
        log.warn(msg);
        Assert.assertNotNull(JUnitAppender.checkForMessage(msg));
        Assert.assertNull(JUnitAppender.checkForMessage(msg));
    }
    
    public void testCheckForMessageInfo() {
        String msg = "Message for testing to find";
        log.error("Dummy");
        Assert.assertNull(JUnitAppender.checkForMessage(msg));
        log.info(msg);
        Assert.assertNotNull(JUnitAppender.checkForMessage(msg));
        Assert.assertNull(JUnitAppender.checkForMessage(msg));
    }

    public void testCheckForMessageStartError() {
        String msg = "Message for testing to find";
        log.error("Dummy");
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
        log.error(msg+" foo");
        Assert.assertNotNull(JUnitAppender.checkForMessageStartingWith(msg));
        // second not match
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
        // check exact match
        log.warn(msg);
        Assert.assertNotNull(JUnitAppender.checkForMessageStartingWith(msg));
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
    }
    
    public void testCheckForMessageStartWarn() {
        String msg = "Message for testing to find";
        log.error("Dummy");
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
        log.warn(msg+" foo");
        Assert.assertNotNull(JUnitAppender.checkForMessageStartingWith(msg));
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
        log.warn(msg);
        Assert.assertNotNull(JUnitAppender.checkForMessageStartingWith(msg));
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
    }
    
    public void testCheckForMessageStartInfo() {
        String msg = "Message for testing to find";
        log.error("Dummy");
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
        log.info(msg+" foo");
        Assert.assertNotNull(JUnitAppender.checkForMessageStartingWith(msg));
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
        log.info(msg);
        Assert.assertNotNull(JUnitAppender.checkForMessageStartingWith(msg));
        Assert.assertNull(JUnitAppender.checkForMessageStartingWith(msg));
    }

    // this is testing how the end of a test works, so continues
    // into the tearDown routine
    boolean testingUnexpected = false;
    boolean cacheFatal;
    boolean cacheError;
    boolean cacheWarn;
    boolean cacheInfo;
    public void testUnexpectedCheck() {
        testingUnexpected = true;
        // cache values
        cacheFatal = JUnitAppender.unexpectedFatalSeen;
        cacheError = JUnitAppender.unexpectedErrorSeen;
        cacheWarn  = JUnitAppender.unexpectedWarnSeen; 
        cacheInfo  = JUnitAppender.unexpectedInfoSeen; 
        
        JUnitAppender.unexpectedFatalSeen = false;
        JUnitAppender.unexpectedErrorSeen = false;
        JUnitAppender.unexpectedWarnSeen  = false; 
        JUnitAppender.unexpectedInfoSeen  = false; 

        Assert.assertFalse("initial FATAL", JUnitAppender.unexpectedMessageSeen(Level.FATAL));
        Assert.assertFalse("initial ERROR", JUnitAppender.unexpectedMessageSeen(Level.ERROR));
        Assert.assertFalse("initial WARN",  JUnitAppender.unexpectedMessageSeen(Level.WARN));
        Assert.assertFalse("initial INFO",  JUnitAppender.unexpectedMessageSeen(Level.INFO));
        
        String msg = "Expected WARN message for testing";
        log.warn(msg);
        JUnitAppender.assertWarnMessage(msg);

        log.info("This INFO message was emitted to test the entire logging chain, please don't remove");
    }

    public void testExpectedWarnMessage() {
        String msg = "Message for testing";
        log.warn(msg);
        JUnitAppender.assertWarnMessage(msg);
    }

    public void testExpectedMessageAsError() {
        String msg = "Message for testing";
        log.error(msg);
        JUnitAppender.assertMessage(msg);
    }

    public void testExpectedMessageAsWarn() {
        String msg = "Message for testing";
        log.warn(msg);
        JUnitAppender.assertMessage(msg);
    }

    public void testExpectedMessageAsInfo() {
        String msg = "Message for testing";
        log.info(msg);
        // JUnitAppender.assertMessage(msg);  // info is usually turned off, so this doesn't pass
    }

    public void testExpectedMessageAsDebug() {
        String msg = "Message for testing";
        log.debug(msg);
        // JUnitAppender.assertMessage(msg);  // debug is usually turned off, so this doesn't pass
    }

    public void testIgnoreLowerBeforeExpectedWarnMessage() {
        log.debug("this is a DEBUG, should still pass");
        log.info("this is an INFO, should still pass");
        log.trace("this is a TRACE, should still pass");
        
        String msg = "Message for testing";
        log.warn(msg);
        JUnitAppender.assertWarnMessage(msg);
    }

    public void testExpectedWarnAfterDebugMessage() {
        String msg = "Message for testing";
        log.debug("debug to skip");
        log.warn(msg);
        JUnitAppender.assertWarnMessage(msg);
    }

    public void testUnexpectedMessage() {
        if (allTests) {
            String msg = "Message should appear in log";
            log.warn(msg);
        }
    }

    public void testClearBacklogDefaultNone() {
        Assert.assertEquals(0,JUnitAppender.clearBacklog());
    }
        
    public void testClearBacklogDefaultWarn() {
        log.warn("warn message");
        Assert.assertEquals(1,JUnitAppender.clearBacklog());
        Assert.assertEquals(0,JUnitAppender.clearBacklog());
    }
        
    public void testClearBacklogDefaultError() {
        log.error("error message");
        Assert.assertEquals(1,JUnitAppender.clearBacklog());
        Assert.assertEquals(0,JUnitAppender.clearBacklog());
    }

    public void testClearBacklogDefaultInfo() {
        log.info("info message");
        Assert.assertEquals(0,JUnitAppender.clearBacklog());
    }

    public void testClearBacklogDefaultMultiple() {
        log.info("info 1");
        log.warn("warn 1");
        log.info("info 2");        
        Assert.assertEquals(1,JUnitAppender.clearBacklog());
        Assert.assertEquals(0,JUnitAppender.clearBacklog());
    }
    
    public void testClearBacklogAtInfoWithInfo() {
        log.info("info message");

        // this test skipped if INFO is not being logged
        if (org.apache.log4j.Category.getRoot().getLevel().toInt() > Level.INFO.toInt()) return;  // redo for Log4J2
        
        Assert.assertEquals(1,JUnitAppender.clearBacklog(org.apache.log4j.Level.INFO));
        Assert.assertEquals(0,JUnitAppender.clearBacklog(org.apache.log4j.Level.INFO));
    }

    public void testClearBacklogAtInfoWithWarn() {
        log.warn("warn message");
        Assert.assertEquals(1,JUnitAppender.clearBacklog(org.apache.log4j.Level.INFO));
        Assert.assertEquals(0,JUnitAppender.clearBacklog(org.apache.log4j.Level.INFO));
    }

    // from here down is testing infrastructure
    public JUnitAppenderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JUnitAppenderTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JUnitAppenderTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {

        apps.tests.Log4JFixture.tearDown();     

        // continue the testUnexpectedCheck test
        if (testingUnexpected) {
            Assert.assertFalse("post FATAL", JUnitAppender.unexpectedMessageSeen(Level.FATAL));
            Assert.assertFalse("post ERROR", JUnitAppender.unexpectedMessageSeen(Level.ERROR));
            Assert.assertFalse("post WARN",  JUnitAppender.unexpectedMessageSeen(Level.WARN));

            Assert.assertTrue("post INFO",  JUnitAppender.unexpectedMessageSeen(Level.INFO));

            JUnitAppender.unexpectedFatalSeen = cacheFatal;
            JUnitAppender.unexpectedErrorSeen = cacheError;
            JUnitAppender.unexpectedWarnSeen  = cacheWarn; 
            JUnitAppender.unexpectedInfoSeen  = cacheInfo; 
            
            testingUnexpected = false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JUnitAppenderTest.class);
}
