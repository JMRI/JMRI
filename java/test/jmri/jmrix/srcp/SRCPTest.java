// SRCPTest.java


package jmri.jmrix.srcp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.srcp package
 * @author			Paul Bender 
 * @version			$Revision$
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
        TestSuite suite = new TestSuite("jmri.jmrix.srcp.SRCPTest");  // no tests in this class itself
        suite.addTest(new TestSuite(SRCPReplyTest.class));
        suite.addTest(new TestSuite(SRCPMessageTest.class));
        suite.addTest(new TestSuite(SRCPTrafficControllerTest.class));
        suite.addTest(new TestSuite(SRCPSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(SRCPCommandStationTest.class));
        suite.addTest(new TestSuite(SRCPTurnoutTest.class));
        suite.addTest(new TestSuite(SRCPSensorTest.class));
        suite.addTest(new TestSuite(SRCPThrottleTest.class));
        suite.addTest(jmri.jmrix.srcp.parser.SRCPClientParserTests.suite());

        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPTest.class.getName());

}
