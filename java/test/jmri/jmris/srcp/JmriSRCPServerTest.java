//SRCPTest.java
package jmri.jmris.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServer class
 *
 * @author Paul Bender
 * @version $Revision$
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(jmri.jmris.srcp.JmriSRCPServerTest.class);

        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(JmriSRCPServerTest.class.getName());

}
