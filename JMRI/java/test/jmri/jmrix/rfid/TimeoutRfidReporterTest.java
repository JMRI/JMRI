package jmri.jmrix.rfid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * TimeoutRfidReporterTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.TimeoutRfidReporter class
 *
 * @author	Paul Bender
 */
public class TimeoutRfidReporterTest extends TestCase {

    public void test1ParamCtor() {
       TimeoutRfidReporter s = new TimeoutRfidReporter("FRA");
       Assert.assertNotNull("exists", s);
    }
    public void test2ParamCtor() {
       TimeoutRfidReporter s = new TimeoutRfidReporter("FRA", "Test");
       Assert.assertNotNull("exists", s);
    }

    // from here down is testing infrastructure
    public TimeoutRfidReporterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TimeoutRfidReporterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TimeoutRfidReporterTest.class);
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
