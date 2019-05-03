package jmri.jmrix.rfid.protocol.olimex;

import jmri.jmrix.AbstractMRReply;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the OlimexRfidProtocol class
 *
 * @author Matthew Harris
 */
public class OlimexRfidProtocolTest {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\r\n-020047C8C3\r\n>");
    AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n+020047C8C3\r\n>");

    /**
     * Test of getMaxSize method, of class OlimexRfidProtocol.
     */
    @Test
    public void testGetMaxSize() {
        assertEquals(16, OlimexRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class OlimexRfidProtocol.
     */
    @Test
    public void testInitString() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals("mc00", instance.initString());
    }

    /**
     * Test of getTag method, of class OlimexRfidProtocol.
     */
    @Test
    public void testGetTag() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals("020047C8C3", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class OlimexRfidProtocol.
     */
    @Test
    public void testProvidesChecksum() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals(false, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class OlimexRfidProtocol.
     */
    @Test
    public void testGetCheckSum() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals("", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class OlimexRfidProtocol.
     */
    @Test
    public void testIsValid() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class OlimexRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetReaderPort() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class OlimexRfidProtocol.
     */
    @Test
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

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
