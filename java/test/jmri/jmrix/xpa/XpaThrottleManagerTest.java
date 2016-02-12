package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XpaThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaThrottleManager class
 *
 * @author	Paul Bender
 * @version $Revision: 17977 $
 */
public class XpaThrottleManagerTest extends TestCase {

    public void testCtor() {
        XpaThrottleManager t = new XpaThrottleManager();
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaThrottleManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaThrottleManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaThrottleManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XpaThrottleManagerTest.class.getName());

}
