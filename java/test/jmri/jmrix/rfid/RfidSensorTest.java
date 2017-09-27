package jmri.jmrix.rfid;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * RfidSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.rfid.RfidSensor class
 *
 * @author	Paul Bender
 */
public class RfidSensorTest extends TestCase {

    public void test1ParamCtor() {
       RfidSensor s = new RfidSensor("FSA");
       Assert.assertNotNull("exists", s);
    }
    public void test2ParamCtor() {
       RfidSensor s = new RfidSensor("FSA", "Test");
       Assert.assertNotNull("exists", s);
    }

    // from here down is testing infrastructure
    public RfidSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RfidSensorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RfidSensorTest.class);
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
