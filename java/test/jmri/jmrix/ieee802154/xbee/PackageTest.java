// PackageTest.java


package jmri.jmrix.ieee802154.xbee;

import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ieee802154.xbee package
 * @author			Paul Bender
 * @version			$Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.ieee802154.xbee.XBeeTest");  // no tests in this class itself
        suite.addTest(new TestSuite(XBeeMessageTest.class));
        suite.addTest(new TestSuite(XBeeReplyTest.class));
        suite.addTest(new TestSuite(XBeeConnectionMemoTest.class));
        suite.addTest(new TestSuite(XBeeTrafficControllerTest.class));
        suite.addTest(new TestSuite(XBeeNodeTest.class));
        suite.addTest(new TestSuite(XBeeSensorManagerTest.class));
        suite.addTest(new TestSuite(XBeeSensorTest.class));
        suite.addTest(new TestSuite(XBeeLightManagerTest.class));
        suite.addTest(new TestSuite(XBeeLightTest.class));
        return suite;
    }

    static Logger log = Logger.getLogger(PackageTest.class.getName());

}
