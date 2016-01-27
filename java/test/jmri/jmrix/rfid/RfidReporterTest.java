package jmri.jmrix.rfid;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RfidReporterTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidReporter class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class RfidReporterTest extends TestCase {

    public void test1ParamCtor() {
       RfidReporter s = new RfidReporter("FRA");
       Assert.assertNotNull("exists", s);
    }
    public void test2ParamCtor() {
       RfidReporter s = new RfidReporter("FRA", "Test");
       Assert.assertNotNull("exists", s);
    }

    // from here down is testing infrastructure
    public RfidReporterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RfidReporterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RfidReporterTest.class);
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

    static Logger log = LoggerFactory.getLogger(RfidReporterTest.class.getName());

}
