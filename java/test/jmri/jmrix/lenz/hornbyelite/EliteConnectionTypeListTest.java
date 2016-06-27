package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * EliteConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.EliteConnectionTypeList class
 *
 * @author	Paul Bender
 */
public class EliteConnectionTypeListTest extends TestCase {

    public void testCtor() {

        EliteConnectionTypeList c = new EliteConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public EliteConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EliteConnectionTypeListTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EliteConnectionTypeListTest.class);
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
