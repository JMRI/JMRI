package jmri.jmrix.lenz;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetPowerManager class
 *
 * @author	Paul Bender
 */
public class XNetPowerManagerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        XNetPowerManager c = new XNetPowerManager(new XNetSystemConnectionMemo(tc));

        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public XNetPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetPowerManagerTest.class);
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
