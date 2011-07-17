// SRCPParserTest.java

package jmri.jmris.srcp.parser;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.io.StringReader;

/**
 * Tests for the {@link jmri.jmris.srcp.parser.SRCPParser} class.
 * @author          Paul Bender
 * @version         $Revision: 1.2 $
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
