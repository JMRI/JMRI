package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Se8AlmImplementationTest extends TestCase {

    public Se8AlmImplementationTest(String s) {
        super(s);
    }

    public void testRW() {
        Se8AlmImplementation alm = new Se8AlmImplementation(4, true);
        alm.setACon(3, 4);
        Assert.assertEquals("ACon", 4, alm.getACon(3));

    }

    LocoNetInterfaceScaffold lnis;

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Se8AlmImplementationTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Se8AlmImplementationTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
