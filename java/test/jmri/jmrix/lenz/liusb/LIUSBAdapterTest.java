package jmri.jmrix.lenz.liusb;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.liusb.LIUSBAdapter class
 *
 * @author	Paul Bender
 */
public class LIUSBAdapterTest extends TestCase {

    public void testCtor() {
        LIUSBAdapter a = new LIUSBAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public LIUSBAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LIUSBAdapterTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LIUSBAdapterTest.class);
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
