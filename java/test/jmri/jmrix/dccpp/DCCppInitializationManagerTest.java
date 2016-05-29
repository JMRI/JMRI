package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppInitializationManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppInitializationManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood
 */
public class DCCppInitializationManagerTest extends TestCase {

    public void testCtor() {

// infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppListenerScaffold l = new DCCppListenerScaffold();

        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(t);

        DCCppInitializationManager m = new DCCppInitializationManager(memo) {
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }
        };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
        //jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down");
    }

    // from here down is testing infrastructure
    public DCCppInitializationManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppInitializationManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppInitializationManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
