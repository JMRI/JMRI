package jmri.jmrix.maple;

import org.junit.Assert;
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.maple.SerialTest");
        suite.addTest(jmri.jmrix.maple.SerialTurnoutTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialSensorManagerTest.class));
        suite.addTest(jmri.jmrix.maple.SerialNodeTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialMessageTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialTrafficControllerTest.suite());
        suite.addTest(jmri.jmrix.maple.SerialAddressTest.suite());
        suite.addTest(jmri.jmrix.maple.OutputBitsTest.suite());
        suite.addTest(jmri.jmrix.maple.InputBitsTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.maple.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.maple.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.maple.packetgen.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.maple.serialmon.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.maple.assignment.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.maple.nodeconfig.PackageTest.class));
        return suite;
    }

}
