//SRCPTest.java

package jmri.jmris.srcp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.srcp package
 * @author                      Paul Bender
 * @version                     $Revision$
 */
public class SRCPTest extends TestCase {

    // from here down is testing infrastructure

    public SRCPTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmris.srcp.SRCPTest");  // no tests in this class itself
        suite.addTest(jmri.jmris.srcp.JmriSRCPServerTest.suite());
        suite.addTest(jmri.jmris.srcp.parser.SRCPParserTests.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPTurnoutServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPSensorServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPPowerServerTest.suite());
        suite.addTest(jmri.jmris.srcp.JmriSRCPProgrammerServerTest.suite());

        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
           // put any tests that require a UI here.
        }

        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPTest.class.getName());

}

