// SRCPTokenizerTest.java

package jmri.jmris.srcp.parser;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.StringReader;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPTokenizer} class.
 * @author          Paul Bender
 * @version         $Revision: 1.2 $
 */
public class SRCPTokenizerTest extends TestCase {

       public void testTokenizePower() {
           String cmd = "POWER ON\n\r";
           SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
           SRCPParserTokenManager stm = new SRCPParserTokenManager(cs);
           Token t = stm.getNextToken();
           assertTrue("Wrong token kind for POWER",SRCPParserConstants.POWER == t.kind);
           t = stm.getNextToken();
           assertTrue("Wrong token kind for ON",SRCPParserConstants.ONOFF == t.kind);
        }
       
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
