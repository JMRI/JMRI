package jmri.jmrix.rfid.protocol.olimex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMRReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the OlimexRfid1356mifareProtocol class based on tests for the
 * OlimexProtocol class
 *
 * @author Matthew Harris
 * @author B. Milhaupt Copyright (C) 2017
 */
public class OlimexRfid1356mifareProtocolTest {

    private final AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\r\n-C4178b55\r\n");
    private final AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n+4178b55C\r\n>");

    /**
     * Test of getMaxSize method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testGetMaxSize() {
        assertEquals(13, OlimexRfid1356mifareProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testInitString() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertEquals("mt100\r\ne0\r\n", instance.initString());
    }

    /**
     * Test of getTag method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testGetTag() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertEquals("C4178b55", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testProvidesChecksum() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertFalse( instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testGetCheckSum() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertEquals("", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testIsValid() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertTrue( instance.isValid(msgStandalone));
        assertFalse( instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testEndOfMessage() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertTrue( instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetReaderPort() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testToMonitorString() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        String expResult = "Reply from Olimex MOD-RFID1356MIFARE reader. Tag read C4178b55";
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
