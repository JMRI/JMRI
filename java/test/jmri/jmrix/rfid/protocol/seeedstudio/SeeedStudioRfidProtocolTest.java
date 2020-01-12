package jmri.jmrix.rfid.protocol.seeedstudio;

import jmri.jmrix.AbstractMRReply;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the SeeedStudioRfidProtocol class
 * 
 * SeeedStudio protocol:
 * 
 * 1-char - [STX] - 0x02
 * 10-chars - ASCII representation of 5 Tag ID bytes
 * 2-chars - ASCII representation of 1 checksum bytes
 * 1-char - [ETX] - 0x03
 * 
 * @author Matthew Harris
 */
public class SeeedStudioRfidProtocolTest {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\u00027800652CC9F8\u0003");
    AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("\u00027800652CC9C6\u0003");

    /**
     * Test of getMaxSize method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testGetMaxSize() {
        assertEquals(14, SeeedStudioRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testInitString() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testGetTag() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals("7800652CC9", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testProvidesChecksum() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals(true, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testGetCheckSum() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals("F8", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testIsValid() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgBadChkSumStandalone));
    }

    /**
     * Test of isCheckSumValid method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testIsCheckSumValid() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals(true, instance.isCheckSumValid(msgStandalone));
        assertEquals(false, instance.isCheckSumValid(msgBadChkSumStandalone));
    }

    /**
     * Test of endOfMessage method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetReaderPort() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testToMonitorString() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        String expResult = "Reply from SeeedStudio reader. Tag read 7800652CC9 checksum F8 valid? yes";
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
