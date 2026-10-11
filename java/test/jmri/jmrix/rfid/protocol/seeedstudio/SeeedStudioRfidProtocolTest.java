package jmri.jmrix.rfid.protocol.seeedstudio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMRReply;

import org.junit.jupiter.api.*;

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

    private final AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\u00027800652CC9F8\u0003");
    private final AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("\u00027800652CC9C6\u0003");

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
        assertTrue( instance.providesChecksum());
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
        assertTrue( instance.isValid(msgStandalone));
        assertFalse( instance.isValid(msgBadChkSumStandalone));
    }

    /**
     * Test of isCheckSumValid method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testIsCheckSumValid() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertTrue( instance.isCheckSumValid(msgStandalone));
        assertFalse( instance.isCheckSumValid(msgBadChkSumStandalone));
    }

    /**
     * Test of endOfMessage method, of class SeeedStudioRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        SeeedStudioRfidProtocol instance = new SeeedStudioRfidProtocol();
        assertTrue( instance.endOfMessage(msgStandalone));
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
