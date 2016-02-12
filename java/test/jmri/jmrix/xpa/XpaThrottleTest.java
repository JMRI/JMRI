package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XpaThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaThrottle class
 *
 * @author	Paul Bender
 * @version $Revision: 17977 $
 */
public class XpaThrottleTest extends TestCase {

    public void testCtor() {
        XpaThrottle t = new XpaThrottle(new jmri.DccLocoAddress(3, false));
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaThrottleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XpaThrottleTest.class.getName());

}
