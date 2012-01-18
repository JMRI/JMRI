// SRCPClientParserTest.java

package jmri.jmrix.srcp.parser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.StringReader;

/**
 * Tests for the {@link jmri.jmrix.srcp.parser.SRCPClientParser} class.
 * @author          Paul Bender
 * @version         $Revision$
 */
public class SRCPClientParserTest extends TestCase {
 
        public void testParseFailure() {
           boolean exceptionOccured = false;
           String code = "POWER SET\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertTrue(exceptionOccured);
        }

       // test valid power commands.
       public void testSetPowerOn(){
           boolean exceptionOccured = false;
           String code = "SET 1 POWER ON\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testSetPowerOff(){
           boolean exceptionOccured = false;
           String code = "SET 1 POWER OFF\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testGetPower(){
           boolean exceptionOccured = false;
           String code = "GET 1 POWER\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testInitPower(){
           boolean exceptionOccured = false;
           String code = "Init 1 POWER\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }
 
      public void testTermPower(){
           boolean exceptionOccured = false;
           String code = "TERM 1 POWER\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       // test valid Feedback (FB) commands.
       public void testGetFB(){
           boolean exceptionOccured = false;
           String code = "GET 1 FB 42\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testSetFB(){
           boolean exceptionOccured = false;
           String code = "SET 1 FB 42 1\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testInitFB(){
           boolean exceptionOccured = false;
           String code = "INIT 1 FB\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testTermFB(){
           boolean exceptionOccured = false;
           String code = "TERM 1 FB\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }
       
	public void testWaitFB(){
           boolean exceptionOccured = false;
           String code = "WAIT 1 FB 42 1 10\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
             pe.printStackTrace();
           }
           assertFalse(exceptionOccured);
       }

       // test valid General Accessory (GA) commands.
       public void testSetGAClosed(){
           boolean exceptionOccured = false;
           String code = "SET 1 GA 42 0 0 -1\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

       public void testSetGAThrown(){
           boolean exceptionOccured = false;
           String code = "SET 1 GA 42 0 1 -1\n\r";
           SRCPClientParser p = new SRCPClientParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertFalse(exceptionOccured);
       }

        // Main entry point
        static public void main(String[] args) {
                String[] testCaseName = {SRCPClientParserTest.class.getName()};
                junit.swingui.TestRunner.main(testCaseName);
        }

        // test suite from all defined tests
        public static Test suite() {
        TestSuite suite = new TestSuite(SRCPClientParserTest.class);
                return suite;

        }
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
