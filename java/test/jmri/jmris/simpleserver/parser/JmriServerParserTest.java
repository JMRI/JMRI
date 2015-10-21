// JmriServerParserTest.java
package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmris.simpleserver.parser.JmriServerParser} class.
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class JmriServerParserTest extends TestCase {

    public void testParseFailure() {
        boolean exceptionOccured = false;
        String code = "POWER SET\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertTrue(exceptionOccured);
    }

    // test valid power commands.
    public void testSetPowerOn() {
        boolean exceptionOccured = false;
        String code = "POWER ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetPowerOff() {
        boolean exceptionOccured = false;
        String code = "POWER OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetPower() {
        boolean exceptionOccured = false;
        String code = "POWER\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

   
    public void testTurnoutProduction() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnout();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTurnoutDeviceProduction() {
        boolean exceptionOccured = false;
        String code = "IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
            p.turnoutdevice();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetTurnoutThrown() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetTurnoutClosed() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetTurnoutStatus() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmriServerParserTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriServerParserTest.class);
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
