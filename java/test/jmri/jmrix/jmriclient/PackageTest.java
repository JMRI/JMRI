package jmri.jmrix.jmriclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.jmriclient package
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
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.jmriclient.JMRiClientTest");  // no tests in this class itself
        suite.addTest(new TestSuite(JMRIClientMessageTest.class));
        suite.addTest(new TestSuite(JMRIClientReplyTest.class));
        suite.addTest(new TestSuite(JMRIClientTurnoutTest.class));
        suite.addTest(new TestSuite(JMRIClientSensorTest.class));
        suite.addTest(new TestSuite(JMRIClientReporterTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientTurnoutManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientSensorManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientReporterManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientTrafficControllerTest.class));
        suite.addTest(new TestSuite(JMRIClientSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientPowerManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jmriclient.networkdriver.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jmriclient.configurexml.PackageTest.class));
        suite.addTest(jmri.jmrix.jmriclient.json.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jmriclient.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientConnectionTypeListTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientLightManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JMRIClientLightTest.class));
        return suite;
    }

}
