// z21Test.java
package jmri.jmrix.roco.z21;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class z21Test extends TestCase {

    // from here down is testing infrastructure
    public z21Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {z21Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.roco.z21.z21Test");  // no tests in this class itself
        suite.addTest(new TestSuite(z21AdapterTest.class));
        suite.addTest(new TestSuite(z21MessageTest.class));
        suite.addTest(new TestSuite(z21ReplyTest.class));
        suite.addTest(new TestSuite(z21TrafficControllerTest.class));
        suite.addTest(new TestSuite(z21SystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(z21XPressNetTunnelTest.class));
        suite.addTest(new TestSuite(z21XNetProgrammerTest.class));
        suite.addTest(new TestSuite(z21XNetThrottleManagerTest.class));
        suite.addTest(new TestSuite(z21XNetThrottleTest.class));
        return suite;
    }

}
