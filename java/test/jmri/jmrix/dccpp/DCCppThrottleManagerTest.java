package jmri.jmrix.dccpp;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppThrottleManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppThrottleManagerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppThrottleManager c = new DCCppThrottleManager(new DCCppSystemConnectionMemo(tc));

        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public DCCppThrottleManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppThrottleManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppThrottleManagerTest.class);
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
