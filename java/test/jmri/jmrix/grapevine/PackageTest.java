package jmri.jmrix.grapevine;

import jmri.util.JUnitUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.grapevine package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007
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
        TestSuite suite = new TestSuite("jmri.jmrix.grapevine.SerialTest");
        suite.addTest(new JUnit4TestAdapter(SerialTurnoutTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialTurnoutTest1.class));
        suite.addTest(new JUnit4TestAdapter(SerialTurnoutTest2.class));
        suite.addTest(new JUnit4TestAdapter(SerialTurnoutTest3.class));
        suite.addTest(new JUnit4TestAdapter(SerialTurnoutManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialLightTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialLightManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialSensorManagerTest.class));
        suite.addTest(SerialNodeTest.suite());
        suite.addTest(SerialMessageTest.suite());
        suite.addTest(SerialReplyTest.suite());
        suite.addTest(new JUnit4TestAdapter(SerialTrafficControllerTest.class));
        suite.addTest(SerialAddressTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.serialdriver.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(GrapevineMenuTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.serialmon.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(GrapevineSystemConnectionMemoTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialPortControllerTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.nodeconfig.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.nodetable.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.packetgen.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialConnectionTypeListTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialSensorTest.class));
        suite.addTest(new JUnit4TestAdapter(SerialSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(jmri.jmrix.grapevine.swing.PackageTest.class));
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
