//JmriServerFrameTest.java
package jmri.jmris;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.JmriServerFrame class 
 *
 * @author Paul Bender
 */
public class JmriServerFrameTest extends TestCase {

    public void testCtorDefault() {
        JmriServerFrame a = new JmriServerFrame();
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JmriServerFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmriServerFrameTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.JmriServerFrameTest.class);

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
