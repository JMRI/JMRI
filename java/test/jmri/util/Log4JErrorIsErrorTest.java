package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Works with the JUnitAppender class to 
 * fail if any FATAL or ERROR messages have been
 * emitted (e.g. not expected)
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class Log4JErrorIsErrorTest extends TestCase {

    public void testNoLog4JMessages() {
        Assert.assertFalse("Unexpected ERROR or FATAL messages emitted", jmri.util.JUnitAppender.unexpectedMessageSeen(org.apache.log4j.Level.ERROR));
    }
    

    // from here down is testing infrastructure
    public Log4JErrorIsErrorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Log4JErrorIsErrorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Log4JErrorIsErrorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Log4JErrorIsErrorTest.class.getName());
}
