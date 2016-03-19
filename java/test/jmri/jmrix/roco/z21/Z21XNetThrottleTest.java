package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetThrottleTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.z21XNetThrottle class
 *
 * @author	Paul Bender
 */
public class Z21XNetThrottleTest extends XNetThrottleTest {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        Z21XNetThrottle t = new Z21XNetThrottle(new XNetSystemConnectionMemo(tc), tc);
        Assert.assertNotNull(t);
    }

    // Test the constructor with an address specified.
    public void testCtorWithArg() throws Exception {
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
        Z21XNetThrottle t = new Z21XNetThrottle(new XNetSystemConnectionMemo(tc), new jmri.DccLocoAddress(3, false), tc);
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public Z21XNetThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Z21XNetThrottleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21XNetThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
