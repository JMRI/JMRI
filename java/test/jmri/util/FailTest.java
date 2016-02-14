// FailTest.java
package jmri.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test that always fails
 *
 * Do not put this in any package-level test suite.  
 * Run this class to test how your testing infrastructure (e.g. CI engine)
 * handles failing tests.
 *
 * @author	Bob Jacobsen Copyright 2015
 */
public class FailTest extends TestCase {

    public void testAlwaysFails() {
        Assert.fail("This test always fails");
    }

    // from here down is testing infrastructure
    public FailTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", FailTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FailTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(FailTest.class.getName());

}
