package jmri.jmris.srcp.parser;

import java.io.StringReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPVisitor} class.
 *
 * @author Paul Bender
 * @version $Revision: 21215 $
 */
public class SRCPVisitorTest extends TestCase {

    public void testCTor() {
        // test the constructor.
        SRCPVisitor v = new SRCPVisitor();
        assertNotNull(v);
    }

    public void testGetServer() {
        // test that an inbound "GET 0 SERVER" returns the
        // expected response.
        boolean exceptionOccured = false;
        String code = "GET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        try {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
            assertEquals(v.getOutputString(), "100 INFO 0 SERVER RUNNING");
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testResetServer() {
        // test that an inbound "RESET 0 SERVER" returns the
        // expected response.
        boolean exceptionOccured = false;
        String code = "RESET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        try {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
            assertEquals(v.getOutputString(), "413 ERROR temporarily prohibited");
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTERMServer() {
        // test that an inbound "TERM 0 SERVER" returns the
        // expected response.
        boolean exceptionOccured = false;
        String code = "TERM 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SRCPVisitor v = new SRCPVisitor();
        try {
            SimpleNode e = p.command();
            e.jjtAccept(v, null);
            assertEquals(v.getOutputString(), "200 OK");
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPVisitorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPVisitorTest.class);
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
