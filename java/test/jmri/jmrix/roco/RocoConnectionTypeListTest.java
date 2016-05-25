package jmri.jmrix.roco;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.roco.RocoConnectionTypeList class
 *
 * @author	Paul Bender
 */
public class RocoConnectionTypeListTest extends TestCase {

    public void testCtor() {

        RocoConnectionTypeList c = new RocoConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public RocoConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RocoConnectionTypeListTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RocoConnectionTypeListTest.class);
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
