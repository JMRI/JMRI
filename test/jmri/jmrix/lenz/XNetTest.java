// XNetTest.java


package jmri.jmrix.lenz;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz package
 * @author			Bob Jacobsen
 * @version			$Revision: 1.5 $
 */
public class XNetTest extends TestCase {

    // from here down is testing infrastructure

    public XNetTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XNetTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.XNetTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XNetMessageTest.class));
        suite.addTest(new TestSuite(XNetTurnoutTest.class));
        suite.addTest(new TestSuite(XNetSensorTest.class));
        suite.addTest(new TestSuite(XNetPacketizerTest.class));
        suite.addTest(new TestSuite(jmri.jmrix.lenz.packetgen.PacketGenFrameTest.class));
        suite.addTest(new TestSuite(XNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(XNetSensorManagerTest.class));
        suite.addTest(new TestSuite(XNetTrafficControllerTest.class));
        suite.addTest(new TestSuite(XNetTrafficRouterTest.class));
        return suite;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTest.class.getName());

}
