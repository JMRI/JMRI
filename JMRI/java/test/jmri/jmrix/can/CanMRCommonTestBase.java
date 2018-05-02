package jmri.jmrix.can;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Common tests for the jmri.jmrix.can.CanMessage and CanReply classes
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class CanMRCommonTestBase extends TestCase {

    // from here down is testing infrastructure
    public CanMRCommonTestBase(String s) {
        super(s);
    }

    // Main entry point - this runs both CanMessage, CanReply
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", CanMRCommonTestBase.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests in CanMessage, CanReply
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.can.CanMRCommonTest");
        suite.addTest(jmri.jmrix.can.CanMessageTest.suite());
        suite.addTest(jmri.jmrix.can.CanReplyTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
