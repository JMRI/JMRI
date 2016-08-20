package jmri.jmrix.rps;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Transform class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class TransformTest extends TestCase {

    public void testBasic() {
    }

    // from here down is testing infrastructure
    public TransformTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TransformTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TransformTest.class);
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
