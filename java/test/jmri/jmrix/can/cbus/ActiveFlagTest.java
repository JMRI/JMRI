package jmri.jmrix.can.cbus;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Description:	tests for the jmri.jmrix.can.cbus.ActiveFlag class
 *
 * @author	Paul Bender
 */
public class ActiveFlagTest extends TestCase {

    public void testIsActive() {
        // is active defalts to false.
        Assert.assertFalse(ActiveFlag.isActive());
    }

    public void testSetActive() {
        ActiveFlag.setActive();
        // is active should be true after setActive() is called.
        Assert.assertTrue(ActiveFlag.isActive());
    }

    // from here down is testing infrastructure
    public ActiveFlagTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ActiveFlagTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ActiveFlagTest.class);
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
