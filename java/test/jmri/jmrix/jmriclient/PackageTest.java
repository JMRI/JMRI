package jmri.jmrix.jmriclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 * @version	$Revision: 18472 $
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
        TestSuite suite = new TestSuite("jmri.jmrix.jmriclient.JMRiClientTest");  // no tests in this class itself
        suite.addTest(new TestSuite(JMRIClientMessageTest.class));
        suite.addTest(new TestSuite(JMRIClientReplyTest.class));
        suite.addTest(new TestSuite(JMRIClientTurnoutTest.class));
        suite.addTest(new TestSuite(JMRIClientSensorTest.class));
        suite.addTest(new TestSuite(JMRIClientReporterTest.class));
        suite.addTest(new TestSuite(JMRIClientTurnoutManagerTest.class));
        suite.addTest(new TestSuite(JMRIClientSensorManagerTest.class));
        suite.addTest(new TestSuite(JMRIClientReporterManagerTest.class));
        suite.addTest(new TestSuite(JMRIClientTrafficControllerTest.class));
        suite.addTest(new TestSuite(JMRIClientSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(JMRIClientPowerManagerTest.class));
        suite.addTest(BundleTest.suite());


        // if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
        // there are currently no swing tests.
        // }
        return suite;
    }

}
