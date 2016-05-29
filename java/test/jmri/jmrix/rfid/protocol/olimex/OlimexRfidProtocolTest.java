package jmri.jmrix.rfid.protocol.olimex;

import jmri.jmrix.AbstractMRReply;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the OlimexRfidProtocol class
 *
 * @author Matthew Harris
 */
public class OlimexRfidProtocolTest extends TestCase {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\r\n-020047C8C3\r\n>");
    AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n+020047C8C3\r\n>");

    /**
     * Test of getMaxSize method, of class OlimexRfidProtocol.
     */
    public void testGetMaxSize() {
        assertEquals(16, OlimexRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class OlimexRfidProtocol.
     */
    public void testInitString() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals("mc00", instance.initString());
    }

    /**
     * Test of getTag method, of class OlimexRfidProtocol.
     */
    public void testGetTag() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals("020047C8C3", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class OlimexRfidProtocol.
     */
    public void testProvidesChecksum() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals(false, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class OlimexRfidProtocol.
     */
    public void testGetCheckSum() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals("", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class OlimexRfidProtocol.
     */
    public void testIsValid() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class OlimexRfidProtocol.
     */
    public void testEndOfMessage() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    public void testGetReaderPort() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class OlimexRfidProtocol.
     */
    public void testToMonitorString() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        String expResult = "Reply from Olimex reader. Tag read 020047C8C3";
        assertEquals(expResult, instance.toMonitorString(msgStandalone));
    }

    class AbstractMRReplyImpl extends AbstractMRReply {

        AbstractMRReplyImpl() {
            super();
        }

        AbstractMRReplyImpl(String s) {
            super(s);
        }

        @Override
        protected int skipPrefix(int index) {
            // doesn't have to do anything
            return index;
        }
    }

    // from here down is testing infrastructure
    public OlimexRfidProtocolTest(String testName) {
        super(testName);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OlimexRfidProtocolTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlimexRfidProtocolTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
