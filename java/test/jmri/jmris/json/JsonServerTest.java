//JsonServerTest.java
package jmri.jmris.json;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.json package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class JsonServerTest extends TestCase {

    public void testCtor() {
        JsonServer a = new JsonServer();
        Assert.assertNotNull(a);
    }

    public void testCtorwithParameter() {
        JsonServer a = new JsonServer(12345,10000);
        //jmri.util.JUnitAppender.assertErrorMessage("Failed to connect to port 12345");
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JsonServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonServerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.json.JsonServerTest.class);
        suite.addTest(jmri.jmris.json.JsonTurnoutServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonPowerServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonReporterServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonSensorServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonLightServerTest.suite());
        suite.addTest(jmri.jmris.json.JsonOperationsServerTest.suite());

        if (!System.getProperty("jmri.headlesstest", "false").equals("true")) {
            // put any tests that require a UI here.
        }

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JsonServerTest.class.getName());

}
