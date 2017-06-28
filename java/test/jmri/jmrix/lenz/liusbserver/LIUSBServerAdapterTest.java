package jmri.jmrix.lenz.liusbserver;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LIUSBServerAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter
 * class
 *
 * @author	Paul Bender
 */
public class LIUSBServerAdapterTest extends TestCase {

    public void testCtor() {
        LIUSBServerAdapter a = new LIUSBServerAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public LIUSBServerAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LIUSBServerAdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LIUSBServerAdapterTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
