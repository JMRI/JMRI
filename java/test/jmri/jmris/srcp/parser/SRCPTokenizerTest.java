package jmri.jmris.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPTokenizer} class.
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
        Assert.assertEquals("Wrong token kind for ZEROADDR", SRCPParserConstants.ZEROADDR, t.kind);
        Assert.assertEquals("Wrong image for ZEROADDR", "0234", t.image);
    }

    @Test
    public void testTokenizeNONZEROADDR() {
        String cmd = "1234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for NONZEROADDR", SRCPParserConstants.NONZEROADDR, t.kind);
        Assert.assertEquals("Wrong image for NONZEROADDR", "1234", t.image);
    }

    // constants.
    @Test
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for ON", SRCPParserConstants.ONOFF == t.kind);
        t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for ON", SRCPParserConstants.ONOFF == t.kind);
    }

    // Device Groups
    @Test
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for POWER", SRCPParserConstants.POWER == t.kind);
        t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for ON", SRCPParserConstants.ONOFF == t.kind);
    }

    @Test
    public void testTokenizeFB() {
        String cmd = "FB\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for FB", SRCPParserConstants.FB == t.kind);
    }

    @Test
    public void testTokenizeGA() {
        String cmd = "GA\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GA", SRCPParserConstants.GA == t.kind);
    }

    @Test
    public void testTokenizeGL() {
        String cmd = "GL\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GL", SRCPParserConstants.GL == t.kind);
    }

    @Test
    public void testTokenizeGM() {
        String cmd = "GM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GM", SRCPParserConstants.GM == t.kind);
    }

    @Test
    public void testTokenizeSM() {
        String cmd = "SM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SM", SRCPParserConstants.SM == t.kind);
    }

    @Test
    public void testTokenizeLOCK() {
        String cmd = "LOCK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for LOCK", SRCPParserConstants.LOCK == t.kind);
    }

    @Test
    public void testTokenizeTIME() {
        String cmd = "TIME\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TIME", SRCPParserConstants.TIME == t.kind);
    }

    @Test
    public void testTokenizeSESSION() {
        String cmd = "SESSION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SESSION", SRCPParserConstants.SESSION == t.kind);
    }

    @Test
    public void testTokenizeDESCRIPTION() {
        String cmd = "DESCRIPTION  \n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for DESCRIPTION", SRCPParserConstants.DESCRIPTION == t.kind);
    }

    @Test
    public void testTokenizeSERVER() {
        String cmd = "SERVER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SERVER", SRCPParserConstants.SERVER == t.kind);
    }

    // commands
    @Test
    public void testTokenizeGET() {
        String cmd = "GET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GET", SRCPParserConstants.GET == t.kind);
    }

    @Test
    public void testTokenizeSET() {
        String cmd = "SET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SET", SRCPParserConstants.SET == t.kind);
    }

    @Test
    public void testTokenizeCHECK() {
        String cmd = "CHECK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for CHECK", SRCPParserConstants.CHECK == t.kind);
    }

    @Test
    public void testTokenizeINIT() {
        String cmd = "INIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for INIT", SRCPParserConstants.INIT == t.kind);
    }

    @Test
    public void testTokenizeTERM() {
        String cmd = "TERM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TERM", SRCPParserConstants.TERM == t.kind);
    }

    @Test
    public void testTokenizeWAIT() {
        String cmd = "WAIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for WAIT", SRCPParserConstants.WAIT == t.kind);
    }

    @Test
    public void testTokenizeVERIFY() {
        String cmd = "VERIFY\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for VERIFY", SRCPParserConstants.VERIFY == t.kind);
    }

    @Test
    public void testTokenizeRESET() {
        String cmd = "RESET\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for RESET", SRCPParserConstants.RESET == t.kind);
    }

    @Test
    public void testTokenizeCV() {
        String cmd = "CV\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for CV", SRCPParserConstants.CV == t.kind);
    }

    @Test
    public void testTokenizeCVBIT() {
        String cmd = "CVBIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for CVBIT", SRCPParserConstants.CVBIT == t.kind);
    }

    @Test
    public void testTokenizeREG() {
        String cmd = "REG\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for REG", SRCPParserConstants.REG == t.kind);
    }

    // This used to be an error.
    // now should check to see that the token produced
    // is the BADTOKEN token.
    @Test
    public void testTokenizeFailure() {
        boolean errorThrown = false;
        String cmd = "this should fail";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
        Token t;
        try {
            t = stm.getNextToken();
            Assert.assertTrue(t.kind == SRCPParserConstants.BADTOKEN);
        } catch (TokenMgrError tme) {
            errorThrown = true;
        }
        Assert.assertFalse(errorThrown);
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
