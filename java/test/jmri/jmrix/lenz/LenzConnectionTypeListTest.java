package jmri.jmrix.lenz;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LenzConnectionTypeListTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.LenzConnectionTypeList class
 *
 * @author	Paul Bender
 */
public class LenzConnectionTypeListTest extends TestCase {

    public void testCtor() {

        LenzConnectionTypeList c = new LenzConnectionTypeList();
        Assert.assertNotNull(c);
    }

    // from here down is testing infrastructure
    public LenzConnectionTypeListTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LenzConnectionTypeListTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LenzConnectionTypeListTest.class);
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
