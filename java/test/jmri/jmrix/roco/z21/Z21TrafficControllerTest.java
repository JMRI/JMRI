package jmri.jmrix.roco.z21;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.z21.z21TrafficController class
 *
 * @author	Paul Bender
 */
public class Z21TrafficControllerTest extends TestCase {

    public void testCtor() {
        Z21TrafficController a = new Z21TrafficController();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public Z21TrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Z21TrafficControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21TrafficControllerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
