// JmriServerTokenizerTest.java
package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmris.simpleserver.parser.JmriServerTokenizer} class.
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class JmriServerTokenizerTest extends TestCase {

    // numeric values 
    public void testTokenizeAddr() {
        String cmd = "1234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertEquals("Wrong token kind for ADDR", JmriServerParserConstants.ADDR, t.kind);
        assertEquals("Wrong image for ADDR", "1234", t.image);
    }

    // constants.
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertEquals("Wrong token kind for ON", JmriServerParserConstants.ONOFF,t.kind);
        t = stm.getNextToken();
        assertEquals("Wrong token kind for OFF", JmriServerParserConstants.ONOFF,t.kind);
    }

    public void testTokenizeTHROWNCLOSED() {
        String cmd = "THROWN CLOSED\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertEquals("Wrong token kind for THROWN", JmriServerParserConstants.THROWNCLOSED,t.kind);
        t = stm.getNextToken();
        assertEquals("Wrong token kind for CLOSED", JmriServerParserConstants.THROWNCLOSED,t.kind);
    }

    // command types
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertEquals("Wrong token kind for POWER", JmriServerParserConstants.POWER,t.kind);
        t = stm.getNextToken();
        assertEquals("Wrong token kind for ON", JmriServerParserConstants.ONOFF,t.kind);
    }

    public void testTokenizeTurnout() {
        String cmd = "TURNOUT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertTrue("Wrong token kind for TURNOUT", JmriServerParserConstants.TURNOUT == t.kind);
    }

    public void testTokenizeReporeter() {
        String cmd = "REPORTER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertTrue("Wrong token kind for REPORTER", JmriServerParserConstants.REPORTER == t.kind);
    }

    public void testTokenizeLight() {
        String cmd = "LIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertTrue("Wrong token kind for LIGHT", JmriServerParserConstants.LIGHT == t.kind);
    }

    public void testTokenizeSENSOR() {
        String cmd = "SENSOR\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertTrue("Wrong token kind for SENSOR", JmriServerParserConstants.SENSOR == t.kind);
    }

    public void testTokenizeThrottle() {
        String cmd = "THROTTLE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertTrue("Wrong token kind for THROTTLE", JmriServerParserConstants.THROTTLE == t.kind);
    }

    public void testTurnoutDevice() {
        String cmd = "IT1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertTrue("Wrong token kind for Turnout Name", JmriServerParserConstants.JMRITURNOUT == t.kind);
    }


    // This used to be an error.
    // now should check to see that the token produced
    // is the BADTOKEN token.
    public void testTokenizeFailure() {
        boolean errorThrown = false;
        String cmd = "this should fail";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t;
        try {
            t = stm.getNextToken();
            assertTrue(t.kind == JmriServerParserConstants.BADTOKEN);
        } catch (TokenMgrError tme) {
            errorThrown = true;
        }
        assertFalse(errorThrown);
    }

    // from here down is testing infrastructure
    public JmriServerTokenizerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JmriServerTokenizerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriServerTokenizerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
