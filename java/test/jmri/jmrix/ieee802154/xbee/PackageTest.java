package jmri.jmrix.ieee802154.xbee;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ieee802154.xbee package
 *
 * @author	Paul Bender
 * @version	$Revision$
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
        TestSuite suite = new TestSuite("jmri.jmrix.ieee802154.xbee.XBeeTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XBeeMessageTest.class));
        suite.addTest(new TestSuite(XBeeReplyTest.class));
        suite.addTest(new TestSuite(XBeeConnectionMemoTest.class));
        suite.addTest(new TestSuite(XBeeTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XBeeNodeTest.class));
        suite.addTest(new TestSuite(XBeeSensorManagerTest.class));
        suite.addTest(new TestSuite(XBeeSensorTest.class));
        suite.addTest(new TestSuite(XBeeLightManagerTest.class));
        suite.addTest(new TestSuite(XBeeLightTest.class));
        suite.addTest(new TestSuite(XBeeTurnoutManagerTest.class));
        suite.addTest(new TestSuite(XBeeTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XBeeAdapterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XBeeNodeManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(XBeeIOStreamTest.class));
        return suite;
    }

}
