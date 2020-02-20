package jmri.jmris.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


/**
 * Tests for the jmri.jmris.srcp.parser.SRCPTokenizer class.
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
public class SRCPTokenizerTest {

    // numeric values 
    @Test
    public void testTokenizeZEROADDR() {
        String cmd = "0234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.ZEROADDR).withFailMessage("Wrong token kind for ZEROADDR");
        assertThat(t.image).isEqualTo("0234").withFailMessage("Wrong image for ZEROADDR");
    }

    @Test
    public void testTokenizeNONZEROADDR() {
        String cmd = "1234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.NONZEROADDR).withFailMessage("Wrong token kind for NONZEROADDR");
        assertThat(t.image).isEqualTo("1234").withFailMessage("Wrong image for NONZEROADDR");
    }

    // constants.
    @Test
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.ONOFF).withFailMessage("Wrong token kind for ON");
        t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.ONOFF).withFailMessage("Wrong token kind for ON");
    }

    // Device Groups
    @Test
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.POWER).withFailMessage("Wrong token kind for POWER");
        t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.ONOFF).withFailMessage("Wrong token kind for ON");
    }

    @Test
    public void testTokenizeFB() {
        String cmd = "FB\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.FB).withFailMessage("Wrong token kind for FB");
    }

    @Test
    public void testTokenizeGA() {
        String cmd = "GA\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.GA).withFailMessage("Wrong token kind for GA");
    }

    @Test
    public void testTokenizeGL() {
        String cmd = "GL\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.GL).withFailMessage("Wrong token kind for GL");
    }

    @Test
    public void testTokenizeGM() {
        String cmd = "GM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.GM).withFailMessage("Wrong token kind for GM");
    }

    @Test
    public void testTokenizeSM() {
        String cmd = "SM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.SM).withFailMessage("Wrong token kind for SM");
    }

    @Test
    public void testTokenizeLOCK() {
        String cmd = "LOCK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.LOCK).withFailMessage("Wrong token kind for LOCK");
    }

    @Test
    public void testTokenizeTIME() {
        String cmd = "TIME\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.TIME).withFailMessage("Wrong token kind for TIME");
    }

    @Test
    public void testTokenizeSESSION() {
        String cmd = "SESSION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.SESSION).withFailMessage("Wrong token kind for SESSION");
    }

    @Test
    public void testTokenizeDESCRIPTION() {
        String cmd = "DESCRIPTION  \n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.DESCRIPTION).withFailMessage("Wrong token kind for DESCRIPTION");
    }

    @Test
    public void testTokenizeSERVER() {
        String cmd = "SERVER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.SERVER).withFailMessage("Wrong token kind for SERVER");
    }

    // commands
    @Test
    public void testTokenizeGET() {
        String cmd = "GET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.GET).withFailMessage("Wrong token kind for GET");
    }

    @Test
    public void testTokenizeSET() {
        String cmd = "SET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.SET).withFailMessage("Wrong token kind for SET");
    }

    @Test
    public void testTokenizeCHECK() {
        String cmd = "CHECK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.CHECK).withFailMessage("Wrong token kind for CHECK");
    }

    @Test
    public void testTokenizeINIT() {
        String cmd = "INIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.INIT).withFailMessage("Wrong token kind for INIT");
    }

    @Test
    public void testTokenizeTERM() {
        String cmd = "TERM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.TERM).withFailMessage("Wrong token kind for TERM");
    }

    @Test
    public void testTokenizeWAIT() {
        String cmd = "WAIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.WAIT).withFailMessage("Wrong token kind for WAIT");
    }

    @Test
    public void testTokenizeVERIFY() {
        String cmd = "VERIFY\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.VERIFY).withFailMessage("Wrong token kind for VERIFY");
    }

    @Test
    public void testTokenizeRESET() {
        String cmd = "RESET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.RESET).withFailMessage("Wrong token kind for RESET");
    }

    @Test
    public void testTokenizeCV() {
        String cmd = "CV\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.CV).withFailMessage("Wrong token kind for CV");
    }

    @Test
    public void testTokenizeCVBIT() {
        String cmd = "CVBIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.CVBIT).withFailMessage("Wrong token kind for CVBIT");
    }

    @Test
    public void testTokenizeREG() {
        String cmd = "REG\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(SRCPParserConstants.REG).withFailMessage("Wrong token kind for REG");
    }

    @Test
    public void testTokenizeFailure() {
        String cmd = "this should fail";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Throwable caught = catchThrowable( () -> stm.getNextToken() );  // called to invoke TokenMgrError
        assertThat(caught).isNotNull().isInstanceOf(TokenMgrError.class);
    }

    // The minimal setup for log4J
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
