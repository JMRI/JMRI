//SRCPTest.java
package jmri.jmris.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServer class
 *
 * @author Paul Bender
 */
public class JmriSRCPServerTest extends TestCase {

    public void testCtor() {
        JmriSRCPServer a = new JmriSRCPServer();
        Assert.assertNotNull(a);
    }

    public void testCtorwithParameter() {
        JmriSRCPServer a = new JmriSRCPServer(2048);
        Assert.assertNotNull(a);
    }

    // from here down is testing infrastructure
    public JmriSRCPServerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.srcp.JmriSRCPServerTest.class);

        return suite;
    }

}
