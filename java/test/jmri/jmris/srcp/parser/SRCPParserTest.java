// SRCPParserTest.java

package jmri.jmris.srcp.parser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.StringReader;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPParser} class.
 * @author          Paul Bender
 * @version         $Revision$
 */
public class SRCPParserTest extends TestCase {

        public void testParseFailure() {
           boolean exceptionOccured = false;
           String code = "POWER SET\n\r";
           SRCPParser p = new SRCPParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
             exceptionOccured = true;
           }
           assertTrue(exceptionOccured);
        }

       public void testSetPowerOn(){
           boolean exceptionOccured = false;
           String code = "SET 1 POWER ON\n\r";
           SRCPParser p = new SRCPParser(new StringReader(code));
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
           SRCPParser p = new SRCPParser(new StringReader(code));
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
           SRCPParser p = new SRCPParser(new StringReader(code));
           try {
             p.command();
           } catch(ParseException pe) {
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
    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
