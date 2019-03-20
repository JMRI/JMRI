package jmri.jmrix.rfid.protocol.olimex;

import jmri.jmrix.AbstractMRReply;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the OlimexRfid1356mifareProtocol class based on tests for the
 * OlimexProtocol class
 *
 * @author Matthew Harris
 * @author B. Milhaupt Copyright (C) 2017
 */
public class OlimexRfid1356mifareProtocolTest {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\r\n-C4178b55\r\n");
    AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n+4178b55C\r\n>");

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
        assertEquals(false, instance.providesChecksum());
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
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class OlimexRfid1356mifareProtocol.
     */
    @Test
    public void testEndOfMessage() {
        OlimexRfid1356mifareProtocol instance = new OlimexRfid1356mifareProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
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
