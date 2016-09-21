package jmri.jmrix.roco.z21.simulator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21.simulator package
 *
 * @author Paul Bender
 */
public class Z21SimulatorTest extends TestCase {

    // from here down is testing infrastructure
    public Z21SimulatorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Z21SimulatorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.roco.z21.simulator.z21SimulatorTest");  // no tests in this class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(Z21SimulatorAdapterTest.class));
        suite.addTest(new TestSuite(Z21XNetSimulatorAdapterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.roco.z21.simulator.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(Z21SimulatorLocoDataTest.class));
        return suite;
    }

}
