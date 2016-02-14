package jmri.jmrix.roco.z21;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * z21TrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.z21TrafficController class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class z21TrafficControllerTest extends TestCase {

    public void testCtor() {
        z21TrafficController a = new z21TrafficController();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public z21TrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", z21TrafficControllerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(z21TrafficControllerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(z21TrafficControllerTest.class.getName());

}
