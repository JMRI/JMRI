package jmri.jmrix.zimo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.zimo package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.zimo.PackageTest");  // no tests in this class itself

        suite.addTest(jmri.jmrix.zimo.swing.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.zimo.mx1.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.zimo.mxulf.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1SystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1PortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1TrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1ExceptionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1MessageExceptionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1CommandStationTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1ConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1MessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1PacketizerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1PowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1ProgrammerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1ProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1ThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1ThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1TurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Mx1TurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
