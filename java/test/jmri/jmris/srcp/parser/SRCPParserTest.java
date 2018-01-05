package jmri.jmris.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPParser} class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
public class SRCPParserTest {

    @Test
    public void testParseFailure() {
        boolean exceptionOccured = false;
        String code = "POWER SET\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertTrue(exceptionOccured);
    }

    // test valid power commands.
    @Test
    public void testSetPowerOn() throws ParseException {
        String code = "SET 1 POWER ON\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Assert.assertNotNull("SET Power On",p.command());
    }

    @Test
    public void testSetPowerOff() throws ParseException {
        String code = "SET 1 POWER OFF\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Assert.assertNotNull("SET Power Off",p.command());
    }

    @Test
    public void testCheckPowerOff() throws ParseException {
        String code = "CHECK 1 POWER OFF\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Assert.assertNotNull("Check Power Off",p.command());
    }

    @Test
    public void testGetPower() {
        boolean exceptionOccured = false;
        String code = "GET 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testInitPower() {
        boolean exceptionOccured = false;
        String code = "Init 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTermPower() {
        boolean exceptionOccured = false;
        String code = "TERM 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test valid Feedback (FB) commands.
    @Test
    public void testGetFB() {
        boolean exceptionOccured = false;
        String code = "GET 1 FB 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetFB() {
        boolean exceptionOccured = false;
        String code = "SET 1 FB 42 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testInitFB() {
        boolean exceptionOccured = false;
        String code = "INIT 1 FB\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTermFB() {
        boolean exceptionOccured = false;
        String code = "TERM 1 FB\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
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
        Assert.assertFalse(exceptionOccured);
    }

    // test valid General Accessory (GA) commands.
    @Test
    public void testSetGAClosed() {
        boolean exceptionOccured = false;
        String code = "SET 1 GA 42 0 0 -1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetGAThrown() {
        boolean exceptionOccured = false;
        String code = "SET 1 GA 42 0 1 -1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testInitGA() {
        boolean exceptionOccured = false;
        String code = "INIT 1 GA 42 N\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    //test SERVER commands
    @Test
    public void testGetServer() {
        boolean exceptionOccured = false;
        String code = "GET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testResetServer() {
        boolean exceptionOccured = false;
        String code = "RESET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTERMServer() {
        boolean exceptionOccured = false;
        String code = "TERM 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test TIME commands
    @Test
    public void testGETTime() {
        boolean exceptionOccured = false;
        String code = "GET 0 TIME\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testINITTime() {
        boolean exceptionOccured = false;
        String code = "INIT 0 TIME 1 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSETTime() {
        boolean exceptionOccured = false;
        String code = "SET 0 TIME 2014014 12 32 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTERMTime() {
        boolean exceptionOccured = false;
        String code = "TERM 0 TIME\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testWaitTime() {
        boolean exceptionOccured = false;
        String code = "WAIT 0 TIME 2014020 1 30 41\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Generic Loco (GL) commands
    @Test
    public void testGetGL() {
        boolean exceptionOccured = false;
        String code = "GET 1 GL 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testInitGLShortAddress() {
        boolean exceptionOccured = false;
        String code = "INIT 1 GL 42 N 1 28 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testInitGLLongAddress() {
        boolean exceptionOccured = false;
        String code = "INIT 1 GL 1042 N 2 28 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetGL() {
        boolean exceptionOccured = false;
        String code = "SET 1 GL 42 0 2 28 0 1 = 0 0 0 0 0\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTERMGL() {
        boolean exceptionOccured = false;
        String code = "TERM 1 GL 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid SESSION commands
    @Test
    public void testGetSESSION() {
        boolean exceptionOccured = false;
        String code = "GET 0 SESSION 12345\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid DESCRITPION commands
    @Test
    public void testGetBusDescription() {
        boolean exceptionOccured = false;
        String code = "GET 0 DESCRIPTION\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetDeviceGroupDescription() {
        boolean exceptionOccured = false;
        String code = "GET 0 DESCRIPTION GA\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetDeviceDescription() {
        boolean exceptionOccured = false;
        String code = "GET 0 DESCRIPTION GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid Service Mode (SM) commands
    @Test
    public void testSetCVValue() {
        boolean exceptionOccured = false;
        String code = "SET 1 SM 0 CV 1 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetCVValue() {
        boolean exceptionOccured = false;
        String code = "GET 1 SM 0 CV 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testINITSM() {
        boolean exceptionOccured = false;
        String code = "INIT 1 SM NMRA\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTERMSM() {
        boolean exceptionOccured = false;
        String code = "TERM 1 SM\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testVerifyCVValue() {
        boolean exceptionOccured = false;
        String code = "VERIFY 1 SM 0 CV 1 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // valid LOCK commands
    @Test
    public void testGetLOCK() {
        boolean exceptionOccured = false;
        String code = "GET 1 LOCK GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetLOCK() {
        boolean exceptionOccured = false;
        String code = "SET 1 LOCK GA 42 60\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTermLOCK() {
        boolean exceptionOccured = false;
        String code = "TERM 1 LOCK GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // handshake mode commands

    // test the "GO" command
    @Test
    public void testGO() throws ParseException {
        String code = "GO\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        Assert.assertNotNull("Go node",n);
    }

    // test the "SET" command
    @Test
    public void testProtocolVersion() throws ParseException {
        String code = "SET PROTOCOL SRCP 1.2.3\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        Assert.assertNotNull("Set node",n);
    }

    @Test
    public void testConnectionModeCommand() throws ParseException {
        String code = "SET CONNECTIONMODE SRCP COMMAND\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        Assert.assertNotNull("Set node",n);
    }

    @Test
    public void testConnectionModeInfo() throws ParseException {
        String code = "SET CONNECTIONMODE SRCP INFO\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        Assert.assertNotNull("Set node",n);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
