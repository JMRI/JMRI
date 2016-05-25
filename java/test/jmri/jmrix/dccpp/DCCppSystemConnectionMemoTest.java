package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppSystemConnectionMemo class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo t = new DCCppSystemConnectionMemo(tc);
        Assert.assertNotNull(t);
        Assert.assertNotNull(t.getDCCppTrafficController());
        // While we are constructing the memo, we should also set the 
        // SystemMemo parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    public void testDCCppTrafficControllerSetCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppSystemConnectionMemo t = new DCCppSystemConnectionMemo();
        Assert.assertNotNull(t);
        // the default constructor does not set the traffic controller
        Assert.assertNull(t.getDCCppTrafficController());
        // so we need to do this ourselves.
        t.setDCCppTrafficController(tc);
        Assert.assertNotNull(t.getDCCppTrafficController());
        // and while we're doing that, we should also set the SystemMemo 
        // parameter in the traffic controller.
        Assert.assertNotNull(tc.getSystemConnectionMemo());
    }

    // from here down is testing infrastructure
    public DCCppSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppSystemConnectionMemoTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppSystemConnectionMemoTest.class);
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
