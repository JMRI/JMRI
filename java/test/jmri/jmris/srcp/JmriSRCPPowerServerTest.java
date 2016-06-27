//JmriSRCPPowerServerTest.java
package jmri.jmris.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPPowerServer class
 *
 * @author Paul Bender
 */
public class JmriSRCPPowerServerTest extends TestCase {

    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });

        JmriSRCPPowerServer a = new JmriSRCPPowerServer(output);
        jmri.util.JUnitAppender.assertErrorMessage("No power manager instance found");
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JmriSRCPPowerServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.srcp.JmriSRCPPowerServerTest.class);

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
