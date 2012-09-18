// SRCPTokenizerTest.java

package jmri.jmris.srcp.parser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.StringReader;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPTokenizer} class.
 * @author          Paul Bender
 * @version         $Revision$
 */
public class SRCPTokenizerTest extends TestCase {

       // numeric values 
       public void testTokenizeZEROADDR() {
           String cmd = "0234\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertEquals("Wrong token kind for ZEROADDR",SRCPParserConstants.ZEROADDR,t.kind);
           assertEquals("Wrong image for ZEROADDR","0234",t.image);
        }

       public void testTokenizeNONZEROADDR() {
           String cmd = "1234\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertEquals("Wrong token kind for NONZEROADDR",SRCPParserConstants.NONZEROADDR,t.kind);
           assertEquals("Wrong image for NONZEROADDR","1234",t.image);
        }

       // constants.
       public void testTokenizeONOFF() {
           String cmd = "ON OFF\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for ON",SRCPParserConstants.ONOFF == t.kind);
           t = stm.getNextToken();
           assertTrue("Wrong token kind for ON",SRCPParserConstants.ONOFF == t.kind);
        }

     // Device Groups
       public void testTokenizePower() {
           String cmd = "POWER ON\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for POWER",SRCPParserConstants.POWER == t.kind);
           t = stm.getNextToken();
           assertTrue("Wrong token kind for ON",SRCPParserConstants.ONOFF == t.kind);
        }

       public void testTokenizeFB() {
           String cmd = "FB\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for FB",SRCPParserConstants.FB == t.kind);
        }
       public void testTokenizeGA() {
           String cmd = "GA\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for GA",SRCPParserConstants.GA == t.kind);
        }
       public void testTokenizeGL() {
           String cmd = "GL\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for GL",SRCPParserConstants.GL == t.kind);
        }
       public void testTokenizeGM() {
           String cmd = "GM\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for GM",SRCPParserConstants.GM == t.kind);
        }
       public void testTokenizeSM() {
           String cmd = "SM\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for SM",SRCPParserConstants.SM == t.kind);
        }
       public void testTokenizeLOCK() {
           String cmd = "LOCK\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for LOCK",SRCPParserConstants.LOCK == t.kind);
        }
       public void testTokenizeTIME() {
           String cmd = "TIME\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for TIME",SRCPParserConstants.TIME == t.kind);
        }
       public void testTokenizeSESSION() {
           String cmd = "SESSION\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for SESSION",SRCPParserConstants.SESSION == t.kind);
        }
       public void testTokenizeDESCRIPTION() {
           String cmd = "DESCRIPTION  \n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for DESCRIPTION",SRCPParserConstants.DESCRIPTION == t.kind);
        }
       public void testTokenizeSERVER() {
           String cmd = "SERVER\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for SERVER",SRCPParserConstants.SERVER == t.kind);
        }

       // commands
       public void testTokenizeGET() {
           String cmd = "GET\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for GET",SRCPParserConstants.GET == t.kind);
        }
       public void testTokenizeSET() {
           String cmd = "SET\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for SET",SRCPParserConstants.SET == t.kind);
        }
       public void testTokenizeCHECK() {
           String cmd = "CHECK\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for CHECK",SRCPParserConstants.CHECK == t.kind);
        }
       public void testTokenizeINIT() {
           String cmd = "INIT\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for INIT",SRCPParserConstants.INIT == t.kind);
        }
       public void testTokenizeTERM() {
           String cmd = "TERM\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for TERM",SRCPParserConstants.TERM == t.kind);
        }
       public void testTokenizeWAIT() {
           String cmd = "WAIT\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for WAIT",SRCPParserConstants.WAIT == t.kind);
        }
       public void testTokenizeVERIFY() {
           String cmd = "VERIFY\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for VERIFY",SRCPParserConstants.VERIFY == t.kind);
        }
       public void testTokenizeRESET() {
           String cmd = "RESET\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for RESET",SRCPParserConstants.RESET == t.kind);
        }
       public void testTokenizeCV() {
           String cmd = "CV\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for CV",SRCPParserConstants.CV == t.kind);
        }
       public void testTokenizeCVBIT() {
           String cmd = "CVBIT\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for CVBIT",SRCPParserConstants.CVBIT == t.kind);
        }
       public void testTokenizeREG() {
           String cmd = "REG\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for REG",SRCPParserConstants.REG == t.kind);
        }

        // error condition.
        public void testTokenizeFailure() {
           boolean errorThrown=false;
           String cmd = "this should fail";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           try {
              stm.getNextToken();
           } catch(TokenMgrError tme) {
              errorThrown=true;
           }
           assertTrue(errorThrown);
       }

        // from here down is testing infrastructure

        public SRCPTokenizerTest(String s) {
                super(s);
        }


        // Main entry point
        static public void main(String[] args) {
                String[] testCaseName = {"-noloading",SRCPTokenizerTest.class.getName()};
                junit.swingui.TestRunner.main(testCaseName);
        }

        // test suite from all defined tests
        public static Test suite() {
            TestSuite suite = new TestSuite(SRCPTokenizerTest.class);
            return suite;
        }
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
