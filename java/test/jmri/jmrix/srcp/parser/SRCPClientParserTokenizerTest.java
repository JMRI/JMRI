package jmri.jmrix.srcp.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.srcp.parser.SRCPClientParserTokenizer class.
 *
 * @author Paul Bender
 */
public class SRCPClientParserTokenizerTest{

    // numeric values
    @Test 
    public void testTokenizeZEROADDR() {
        String cmd = "0234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for ZEROADDR", SRCPClientParserConstants.ZEROADDR, t.kind);
        Assert.assertEquals("Wrong image for ZEROADDR", "0234", t.image);
    }

    @Test 
    public void testTokenizeNONZEROADDR() {
        String cmd = "1234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for NONZEROADDR", SRCPClientParserConstants.NONZEROADDR, t.kind);
        Assert.assertEquals("Wrong image for NONZEROADDR", "1234", t.image);
    }

    // constants.
    @Test 
    public void testTokenizeON() {
        String cmd = "ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for ON", SRCPClientParserConstants.ONOFF == t.kind);
    }

    @Test 
    public void testTokenizeOFf() {
        String cmd = "OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for OFF", SRCPClientParserConstants.ONOFF == t.kind);
    }

    // Device Groups
    @Test 
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for POWER", SRCPClientParserConstants.POWER == t.kind);
        t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for ON", SRCPClientParserConstants.ONOFF == t.kind);
    }

    @Test 
    public void testTokenizeFB() {
        String cmd = "FB\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for FB", SRCPClientParserConstants.FB == t.kind);
    }

    @Test 
    public void testTokenizeGA() {
        String cmd = "GA\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GA", SRCPClientParserConstants.GA == t.kind);
    }

    @Test 
    public void testTokenizeGL() {
        String cmd = "GL\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GL", SRCPClientParserConstants.GL == t.kind);
    }

    @Test 
    public void testTokenizeGM() {
        String cmd = "GM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for GM", SRCPClientParserConstants.GM == t.kind);
    }

    @Test 
    public void testTokenizeSM() {
        String cmd = "SM\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SM", SRCPClientParserConstants.SM == t.kind);
    }

    @Test 
    public void testTokenizeLOCK() {
        String cmd = "LOCK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for LOCK", SRCPClientParserConstants.LOCK == t.kind);
    }

    @Test 
    public void testTokenizeTIME() {
        String cmd = "TIME\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TIME", SRCPClientParserConstants.TIME == t.kind);
    }

    @Test 
    public void testTokenizeSESSION() {
        String cmd = "SESSION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SESSION", SRCPClientParserConstants.SESSION == t.kind);
    }

    @Test 
    public void testTokenizeDESCRIPTION() {
        String cmd = "DESCRIPTION  \n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for DESCRIPTION", SRCPClientParserConstants.DESCRIPTION == t.kind);
    }

    @Test 
    public void testTokenizeSERVER() {
        String cmd = "SERVER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SERVER", SRCPClientParserConstants.SERVER == t.kind);
    }

    @Test 
    public void testTokenizeCOMMAND() {
        String cmd = "COMMAND\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for COMMAND", SRCPClientParserConstants.COMMAND == t.kind);
    }

    @Test 
    public void testTokenizeINFO() {
        String cmd = "INFO\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for INFO", SRCPClientParserConstants.INFO == t.kind);
    }

    @Test 
    public void testTokenizeERROR() {
        String cmd = "ERROR\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for ERROR", SRCPClientParserConstants.ERROR == t.kind);
    }

    @Test 
    public void testTokenizeOK() {
        String cmd = "OK\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for OK", SRCPClientParserConstants.OK == t.kind);
    }

    @Test 
    public void testTokenizeSRCP() {
        String cmd = "SRCP\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SRCP", SRCPClientParserConstants.SRCP == t.kind);
    }

    @Test 
    public void testTokenizeVERSION() {
        String cmd = "0.8.3\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for VERSION", SRCPClientParserConstants.VERSION == t.kind);
    }

    @Test 
    public void testTokenizeCV() {
        String cmd = "CV\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for CV", SRCPClientParserConstants.CV == t.kind);
    }

    @Test 
    public void testTokenizeCVBIT() {
        String cmd = "CVBIT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for CVBIT", SRCPClientParserConstants.CVBIT == t.kind);
    }

    @Test 
    public void testTokenizeREG() {
        String cmd = "REG\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for REG", SRCPClientParserConstants.REG == t.kind);
    }

    // error condition.
    @Test(expected=TokenMgrError.class) 
    public void testTokenizeFailure() throws TokenMgrError {
        String cmd = "this should fail";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        SRCPClientParserTokenManager stm = new SRCPClientParserTokenManager(cs);
        stm.getNextToken();
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
