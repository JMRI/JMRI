//JsonServerTest.java
package jmri.jmris.json;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.json package
 *
 * @author Paul Bender
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
        TestSuite suite = new TestSuite(PackageTest.class.getPackage().getName());
        suite.addTest(new JUnit4TestAdapter(JsonServerTest.class));
        suite.addTest(jmri.jmris.json.JsonTurnoutServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonPowerServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonReporterServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonSensorServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonLightServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonOperationsServerTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonExceptionTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerActionTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerPreferencesPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerPreferencesTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonUtilTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonConnectionTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonClientHandlerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonConsistServerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonProgrammerServerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonRosterServerTest.class)); 
        suite.addTest(new JUnit4TestAdapter(JsonRouteServerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonSignalHeadServerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonSignalMastServerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonThrottleServerTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonThrottleTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonTimeServerTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}

