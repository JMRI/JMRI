// JUnitAppenderTest.java
package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.util.JUnitAppender class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
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

    public void testExpectedWarnMessage() {
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

    // from here down is testing infrastructure
    public JUnitAppenderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JUnitAppenderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JUnitAppenderTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JUnitAppenderTest.class.getName());
}
