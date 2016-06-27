package jmri.jmrix.sprog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SprogCSStreamPortControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.sprog.SprogCSStreamPortController class
 *
 * @author	Paul Bender
 */
public class SprogCSStreamPortControllerTest extends TestCase {

    public void testCtor() {

        try {
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            DataOutputStream ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
            tempPipe = new PipedInputStream();
            DataInputStream istream = new DataInputStream(tempPipe);

            SprogCSStreamPortController xspc = new SprogCSStreamPortController(istream, ostream, "Test");
            Assert.assertNotNull("exists", xspc);
        } catch (java.io.IOException ioe) {
            Assert.fail("IOException creating stream");
        }
    }

    // from here down is testing infrastructure
    public SprogCSStreamPortControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SprogCSStreamPortControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SprogCSStreamPortControllerTest.class);
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

}
