package jmri.jmrix.xpa;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Description:	tests for the jmri.jmrix.xpa.XpaThrottle class
 * <P>
 * @author	Paul Bender
 */
public class XpaThrottleTest extends TestCase {

    private XpaTrafficController tc = null;

    public void testCtor() {
        XpaThrottle t = new XpaThrottle(new jmri.DccLocoAddress(3, false),tc);
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaThrottleTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XpaTrafficController();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        tc = null;
    }

}
