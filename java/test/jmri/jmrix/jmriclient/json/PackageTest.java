package jmri.jmrix.jmriclient.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 * 
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
        TestSuite suite = new TestSuite("jmri.jmrix.jmriclient.json.PackageTest");  // no tests in this class itself
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jmriclient.json.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jmriclient.json.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonNetworkConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonNetworkPortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientReplyTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientLightManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientLightTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientMessageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonClientPowerManagerTest.class));

        return suite;
    }

}
