package jmri.jmrix.powerline;

//import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.powerline package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.powerline.SerialTest");
        suite.addTest(X10SequenceTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialSensorManagerTest.class));
        suite.addTest(SerialNodeTest.suite());
        suite.addTest(SerialAddressTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.cm11.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.insteon2412s.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.simulator.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.cp290.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.serialdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.powerline.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(InsteonSequenceTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialSensorTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialX10LightTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SystemMenuTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
