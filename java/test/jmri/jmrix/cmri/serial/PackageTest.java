// PackageTest.java
package jmri.jmrix.cmri.serial;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.cmri.serial package.
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
        TestSuite suite = new TestSuite("jmri.jmrix.cmri.serial.SerialTest");

        suite.addTest(jmri.jmrix.cmri.serial.SerialTurnoutTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialTurnoutManagerTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialSensorManagerTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialNodeTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialMessageTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.cmri.serial.SerialAddressTest.suite());
        return suite;
    }

}
