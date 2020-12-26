package jmri.jmris.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPParser} class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
public class SRCPParserTest {

    @Test
    public void testParseFailure() {
        
        String code = "POWER SET\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNotNull().isInstanceOf(ParseException.class);
    }

    // test valid power commands.
    @Test
    public void testSetPowerOn() throws ParseException {
        String code = "SET 1 POWER ON\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        assertThat(p.command()).withFailMessage("SET Power On").isNotNull();
    }

    @Test
    public void testSetPowerOff() throws ParseException {
        String code = "SET 1 POWER OFF\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        assertThat(p.command()).withFailMessage("SET Power Off").isNotNull();
    }

    @Test
    public void testCheckPowerOff() throws ParseException {
        String code = "CHECK 1 POWER OFF\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        assertThat(p.command()).withFailMessage("Check Power Off").isNotNull();
    }

    @Test
    public void testGetPower() {
        String code = "GET 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testInitPower() {
        String code = "Init 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTermPower() {
        String code = "TERM 1 POWER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // test valid Feedback (FB) commands.
    @Test
    public void testGetFB() {
        String code = "GET 1 FB 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSetFB() {
        String code = "SET 1 FB 42 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testInitFB() {
        String code = "INIT 1 FB\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTermFB() {
        String code = "TERM 1 FB\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testWaitFB() {
        String code = "WAIT 1 FB 42 1 10\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // test valid General Accessory (GA) commands.
    @Test
    public void testSetGAClosed() {
        String code = "SET 1 GA 42 0 0 -1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSetGAThrown() {
        String code = "SET 1 GA 42 0 1 -1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testInitGA() {
        String code = "INIT 1 GA 42 N\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    //test SERVER commands
    @Test
    public void testGetServer() {
        String code = "GET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testResetServer() {
        String code = "RESET 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTERMServer() {
        String code = "TERM 0 SERVER\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // test TIME commands
    @Test
    public void testGETTime() {
        String code = "GET 0 TIME\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testINITTime() {
        String code = "INIT 0 TIME 1 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSETTime() {
        String code = "SET 0 TIME 2014014 12 32 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTERMTime() {
        String code = "TERM 0 TIME\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testWaitTime() {
        String code = "WAIT 0 TIME 2014020 1 30 41\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // valid Generic Loco (GL) commands
    @Test
    public void testGetGL() {
        String code = "GET 1 GL 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testInitGLShortAddress() {
        String code = "INIT 1 GL 42 N 1 28 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testInitGLLongAddress() {
        String code = "INIT 1 GL 1042 N 2 28 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSetGL() {
        String code = "SET 1 GL 42 0 2 28 0 1 = 0 0 0 0 0\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTERMGL() {
        String code = "TERM 1 GL 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // valid SESSION commands
    @Test
    public void testGetSESSION() {
        String code = "GET 0 SESSION 12345\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // valid DESCRITPION commands
    @Test
    public void testGetBusDescription() {
        String code = "GET 0 DESCRIPTION\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testGetDeviceGroupDescription() {
        String code = "GET 0 DESCRIPTION GA\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testGetDeviceDescription() {
        String code = "GET 0 DESCRIPTION GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // valid Service Mode (SM) commands
    @Test
    public void testSetCVValue() {
        String code = "SET 1 SM 0 CV 1 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testGetCVValue() {
        String code = "GET 1 SM 0 CV 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSetCVBITValue() {
        String code = "SET 1 SM 0 CVBIT 1 1 0\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testGetCVBITValue() {
        String code = "GET 1 SM 0 CVBIT 1 0\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSetRegValue() {
        String code = "SET 1 SM 0 REG 1 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testGetRegValue() {
        String code = "GET 1 SM 0 REG 1\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }


    @Test
    public void testINITSM() {
        String code = "INIT 1 SM NMRA\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTERMSM() {
        String code = "TERM 1 SM\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testVerifyCVValue() {
        String code = "VERIFY 1 SM 0 CV 1 2\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // valid LOCK commands
    @Test
    public void testGetLOCK() {
        String code = "GET 1 LOCK GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSetLOCK() {
        String code = "SET 1 LOCK GA 42 60\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    @Test
    public void testTermLOCK() {
        String code = "TERM 1 LOCK GA 42\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        assertThat(thrown).isNull();
    }

    // handshake mode commands

    // test the "GO" command
    @Test
    public void testGO() throws ParseException {
        String code = "GO\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        assertThat(n).withFailMessage("Go node").isNotNull();
    }

    // test the "SET" command
    @Test
    public void testProtocolVersion() throws ParseException {
        String code = "SET PROTOCOL SRCP 1.2.3\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        assertThat(n).withFailMessage("Set node").isNotNull();
    }

    @Test
    public void testConnectionModeCommand() throws ParseException {
        String code = "SET CONNECTIONMODE SRCP COMMAND\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        assertThat(n).withFailMessage("Set node").isNotNull();
    }

    @Test
    public void testConnectionModeInfo() throws ParseException {
        String code = "SET CONNECTIONMODE SRCP INFO\n\r";
        SRCPParser p = new SRCPParser(new StringReader(code));
        SimpleNode n = p.handshakecommand();
        assertThat(n).withFailMessage("Set node").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
