package jmri.jmrix.lenz.xnetsimulator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetSimulatorAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.xnetsimulator.XNetSimulatorAdapter
 * class
 *
 * @author	Paul Bender
 */
public class XNetSimulatorAdapterTest extends TestCase {

    public void testCtor() {
        XNetSimulatorAdapter a = new XNetSimulatorAdapter();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public XNetSimulatorAdapterTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetSimulatorAdapterTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetSimulatorAdapterTest.class);
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
