package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppConnectionTypeList class
 *
 * @author	Paul Bender
 * @author	Mark Underwood
 */
public class DCCppConnectionTypeListTest extends TestCase {

    public void testCtor() {

        DCCppConnectionTypeList c = new DCCppConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public DCCppConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppConnectionTypeListTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppConnectionTypeListTest.class);
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
