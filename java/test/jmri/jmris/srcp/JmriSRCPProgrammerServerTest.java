//JmriSRCPProgrammerServerTest.java
package jmri.jmris.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPProgrammerServer class
 *
 * @author Paul Bender
 */
public class JmriSRCPProgrammerServerTest extends TestCase {

    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        JmriSRCPProgrammerServer a = new JmriSRCPProgrammerServer(output);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JmriSRCPProgrammerServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.srcp.JmriSRCPProgrammerServerTest.class);

        return suite;
    }

}
