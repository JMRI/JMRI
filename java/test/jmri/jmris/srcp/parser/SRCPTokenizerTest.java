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
        assertThat(t.kind).withFailMessage("Wrong token kind for ZEROADDR").isEqualTo(SRCPParserConstants.ZEROADDR);
        assertThat(t.image).withFailMessage("Wrong image for ZEROADDR").isEqualTo("0234");
    }

    @Test
    public void testTokenizeNONZEROADDR() {
        String cmd = "1234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for NONZEROADDR").isEqualTo(SRCPParserConstants.NONZEROADDR);
        assertThat(t.image).withFailMessage("Wrong image for NONZEROADDR").isEqualTo("1234");
    }

    // constants.
    @Test
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for ON").isEqualTo(SRCPParserConstants.ONOFF);
        t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for ON").isEqualTo(SRCPParserConstants.ONOFF);
    }

    // Device Groups
    @Test
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for POWER").isEqualTo(SRCPParserConstants.POWER);
        t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for ON").isEqualTo(SRCPParserConstants.ONOFF);
    }

    @Test
    public void testTokenizeFB() {
        String cmd = "FB\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for FB").isEqualTo(SRCPParserConstants.FB);
    }

    @Test
    public void testTokenizeGA() {
        String cmd = "GA\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for GA").isEqualTo(SRCPParserConstants.GA);
    }

    @Test
    public void testTokenizeGL() {
        String cmd = "GL\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for GL").isEqualTo(SRCPParserConstants.GL);
    }

    @Test
    public void testTokenizeGM() {
        String cmd = "GM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for GM").isEqualTo(SRCPParserConstants.GM);
    }

    @Test
    public void testTokenizeSM() {
        String cmd = "SM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for SM").isEqualTo(SRCPParserConstants.SM);
    }

    @Test
    public void testTokenizeLOCK() {
        String cmd = "LOCK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for LOCK").isEqualTo(SRCPParserConstants.LOCK);
    }

    @Test
    public void testTokenizeTIME() {
        String cmd = "TIME\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TIME").isEqualTo(SRCPParserConstants.TIME);
    }

    @Test
    public void testTokenizeSESSION() {
        String cmd = "SESSION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for SESSION").isEqualTo(SRCPParserConstants.SESSION);
    }

    @Test
    public void testTokenizeDESCRIPTION() {
        String cmd = "DESCRIPTION  \n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for DESCRIPTION").isEqualTo(SRCPParserConstants.DESCRIPTION);
    }

    @Test
    public void testTokenizeSERVER() {
        String cmd = "SERVER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for SERVER").isEqualTo(SRCPParserConstants.SERVER);
    }

    // commands
    @Test
    public void testTokenizeGET() {
        String cmd = "GET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for GET").isEqualTo(SRCPParserConstants.GET);
    }

    @Test
    public void testTokenizeSET() {
        String cmd = "SET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for SET").isEqualTo(SRCPParserConstants.SET);
    }

    @Test
    public void testTokenizeCHECK() {
        String cmd = "CHECK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for CHECK").isEqualTo(SRCPParserConstants.CHECK);
    }

    @Test
    public void testTokenizeINIT() {
        String cmd = "INIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for INIT").isEqualTo(SRCPParserConstants.INIT);
    }

    @Test
    public void testTokenizeTERM() {
        String cmd = "TERM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TERM").isEqualTo(SRCPParserConstants.TERM);
    }

    @Test
    public void testTokenizeWAIT() {
        String cmd = "WAIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for WAIT").isEqualTo(SRCPParserConstants.WAIT);
    }

    @Test
    public void testTokenizeVERIFY() {
        String cmd = "VERIFY\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for VERIFY").isEqualTo(SRCPParserConstants.VERIFY);
    }

    @Test
    public void testTokenizeRESET() {
        String cmd = "RESET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for RESET").isEqualTo(SRCPParserConstants.RESET);
    }

    @Test
    public void testTokenizeCV() {
        String cmd = "CV\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for CV").isEqualTo(SRCPParserConstants.CV);
    }

    @Test
    public void testTokenizeCVBIT() {
        String cmd = "CVBIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for CVBIT").isEqualTo(SRCPParserConstants.CVBIT);
    }

    @Test
    public void testTokenizeREG() {
        String cmd = "REG\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for REG").isEqualTo(SRCPParserConstants.REG);
    }

    @Test
    public void testTokenizeFailure() {
        String cmd = "this should fail";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Throwable caught = catchThrowable(stm::getNextToken);  // called to invoke TokenMgrError
        assertThat(caught).isNotNull().isInstanceOf(TokenMgrError.class);
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
