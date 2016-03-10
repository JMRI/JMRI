// z21SimulatorTest.java
package jmri.jmrix.roco.z21.simulator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21.simulator package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class z21SimulatorTest extends TestCase {

    // from here down is testing infrastructure
    public z21SimulatorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {z21SimulatorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.roco.z21.simulator.z21SimulatorTest");  // no tests in this class itself
        suite.addTest(new TestSuite(z21XNetSimulatorAdapterTest.class));
        return suite;
    }

}
