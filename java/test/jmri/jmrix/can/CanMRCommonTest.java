package jmri.jmrix.can;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Common tests for the jmri.jmrix.can.CanMessage and CanReply classes
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class CanMRCommonTest extends TestCase {

    // from here down is testing infrastructure
    public CanMRCommonTest(String s) {
        super(s);
    }

    // Main entry point - this runs both CanMessage, CanReply
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", CanMRCommonTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests in CanMessage, CanReply
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.can.CanMRCommonTest");
        suite.addTest(jmri.jmrix.can.CanMessageTest.suite());
        suite.addTest(jmri.jmrix.can.CanReplyTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
