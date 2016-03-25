package jmri.jmris.srcp.parser;

import java.io.StringReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPParser} class.
 *
 * @author Paul Bender
 */
public class SRCPParserTest extends TestCase {

    public void testParseFailure() {
        boolean exceptionOccured = false;
        String code = "POWER SET\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
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
        String code = "SET 1 POWER ON\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetPowerOff() {
        boolean exceptionOccured = false;
        String code = "SET 1 POWER OFF\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetPower() {
        boolean exceptionOccured = false;
        String code = "GET 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testInitPower() {
        boolean exceptionOccured = false;
        String code = "Init 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTermPower() {
        boolean exceptionOccured = false;
        String code = "TERM 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // test valid Feedback (FB) commands.
    public void testGetFB() {
        boolean exceptionOccured = false;
        String code = "GET 1 FB 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetFB() {
        boolean exceptionOccured = false;
        String code = "SET 1 FB 42 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testInitFB() {
        boolean exceptionOccured = false;
        String code = "INIT 1 FB\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTermFB() {
        boolean exceptionOccured = false;
        String code = "TERM 1 FB\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testWaitFB() {
        boolean exceptionOccured = false;
        String code = "WAIT 1 FB 42 1 10\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
            pe.printStackTrace();
        }
        assertFalse(exceptionOccured);
    }

    // test valid General Accessory (GA) commands.
    public void testSetGAClosed() {
        boolean exceptionOccured = false;
        String code = "SET 1 GA 42 0 0 -1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetGAThrown() {
        boolean exceptionOccured = false;
        String code = "SET 1 GA 42 0 1 -1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testInitGA() {
        boolean exceptionOccured = false;
        String code = "INIT 1 GA 42 N\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    //test SERVER commands
    public void testGetServer() {
        boolean exceptionOccured = false;
        String code = "GET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testResetServer() {
        boolean exceptionOccured = false;
        String code = "RESET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTERMServer() {
        boolean exceptionOccured = false;
        String code = "TERM 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // test TIME commands
    public void testGETTime() {
        boolean exceptionOccured = false;
        String code = "GET 0 TIME\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testINITTime() {
        boolean exceptionOccured = false;
        String code = "INIT 0 TIME 1 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSETTime() {
        boolean exceptionOccured = false;
        String code = "SET 0 TIME 2014014 12 32 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTERMTime() {
        boolean exceptionOccured = false;
        String code = "TERM 0 TIME\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testWaitTime() {
        boolean exceptionOccured = false;
        String code = "WAIT 0 TIME 2014020 1 30 41\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // valid Generic Loco (GL) commands
    public void testGetGL() {
        boolean exceptionOccured = false;
        String code = "GET 1 GL 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testInitGLShortAddress() {
        boolean exceptionOccured = false;
        String code = "INIT 1 GL 42 N 1 28 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testInitGLLongAddress() {
        boolean exceptionOccured = false;
        String code = "INIT 1 GL 1042 N 2 28 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetGL() {
        boolean exceptionOccured = false;
        String code = "SET 1 GL 42 0 2 28 0 1 = 0 0 0 0 0\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTERMGL() {
        boolean exceptionOccured = false;
        String code = "TERM 1 GL 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // valid SESSION commands
    public void testGetSESSION() {
        boolean exceptionOccured = false;
        String code = "GET 0 SESSION 12345\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // valid DESCRITPION commands
    public void testGetBusDescription() {
        boolean exceptionOccured = false;
        String code = "GET 0 DESCRIPTION\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetDeviceGroupDescription() {
        boolean exceptionOccured = false;
        String code = "GET 0 DESCRIPTION GA\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetDeviceDescription() {
        boolean exceptionOccured = false;
        String code = "GET 0 DESCRIPTION GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SRCPParserTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPParserTest.class);
        return suite;

    }

    // valid Service Mode (SM) commands
    public void testSetCVValue() {
        boolean exceptionOccured = false;
        String code = "SET 1 SM 0 CV 1 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetCVValue() {
        boolean exceptionOccured = false;
        String code = "GET 1 SM 0 CV 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testINITSM() {
        boolean exceptionOccured = false;
        String code = "INIT 1 SM NMRA\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTERMSM() {
        boolean exceptionOccured = false;
        String code = "TERM 1 SM\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testVerifyCVValue() {
        boolean exceptionOccured = false;
        String code = "VERIFY 1 SM 0 CV 1 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // valid LOCK commands
    public void testGetLOCK() {
        boolean exceptionOccured = false;
        String code = "GET 1 LOCK GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetLOCK() {
        boolean exceptionOccured = false;
        String code = "SET 1 LOCK GA 42 60\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTermLOCK() {
        boolean exceptionOccured = false;
        String code = "TERM 1 LOCK GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
