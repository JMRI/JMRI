// PackageTest.java
package jmri.jmrix.maple;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.maple package.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    public void testDefinitions() {
        Assert.assertEquals("Node definitions match", SerialSensorManager.SENSORSPERUA,
                SerialNode.MAXSENSORS + 1);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.maple.SerialTest");
        suite.addTest(jmri.jmrix.maple.SerialTurnoutTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialNodeTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialMessageTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialAddressTest.suite());
        suite.addTest(jmri.jmrix.maple.OutputBitsTest.suite());
        suite.addTest(jmri.jmrix.maple.InputBitsTest.suite());
        return suite;
    }

}
