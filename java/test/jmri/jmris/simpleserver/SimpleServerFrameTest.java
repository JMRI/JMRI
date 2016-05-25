//SimpleServerFrameTest.java
package jmri.jmris.simpleserver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerFrame class 
 *
 * @author Paul Bender
 */
public class SimpleServerFrameTest extends TestCase {

    public void testCtorDefault() {
        SimpleServerFrame a = new SimpleServerFrame();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public SimpleServerFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SimpleServerFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.simpleserver.SimpleServerFrameTest.class);

        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
