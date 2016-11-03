package jmri.jmrix.oaktree;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.oaktree package.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    public void testDefinitions() {
        Assert.assertEquals("Node definitions match", SerialSensorManager.SENSORSPERNODE,
                SerialNode.MAXSENSORS + 1);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.oaktree.SerialTest");
        suite.addTest(SerialTurnoutTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialSensorManagerTest.class));
        suite.addTest(SerialNodeTest.suite());
        suite.addTest(SerialMessageTest.suite());
        suite.addTest(SerialTrafficControllerTest.suite());
        suite.addTest(SerialAddressTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.oaktree.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.oaktree.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.oaktree.nodeconfig.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.oaktree.packetgen.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.oaktree.serialmon.PackageTest.class));
        return suite;
    }

}
