package jmri.jmrix.rfid.protocol.olimex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMRReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the OlimexRfidProtocol class
 *
 * @author Matthew Harris
 */
public class OlimexRfidProtocolTest {

    private final AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\r\n-020047C8C3\r\n>");
    private final AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n+020047C8C3\r\n>");

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
        assertFalse( instance.providesChecksum());
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
        assertTrue( instance.isValid(msgStandalone));
        assertFalse( instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class OlimexRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        OlimexRfidProtocol instance = new OlimexRfidProtocol();
        assertTrue( instance.endOfMessage(msgStandalone));
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

    private static class AbstractMRReplyImpl extends AbstractMRReply {

        AbstractMRReplyImpl(String s) {
            super(s);
        }

        @Override
        protected int skipPrefix(int index) {
            // doesn't have to do anything
            return index;
        }
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
