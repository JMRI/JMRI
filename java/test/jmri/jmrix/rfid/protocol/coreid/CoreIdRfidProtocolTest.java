package jmri.jmrix.rfid.protocol.coreid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMRReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the CoreIdRfidProtocol class
 *
 * @author Matthew Harris
 */
public class CoreIdRfidProtocolTest {

    private final AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\u000204171F04FEF6\r\n\u0003");
    private final AbstractMRReply msgConcentrator = new AbstractMRReplyImpl("A04171F04FEF6\r\n>");
    private final AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("\u000204171F04FEF7\r\n\u0003");
    private final AbstractMRReply msgBadChkSumConcentrator = new AbstractMRReplyImpl("A04171F04FEF7\r\n>");

    /**
     * Test of getMaxSize method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetMaxSize() {
        assertEquals(16, CoreIdRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testInitString() {
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetTag() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals("04171F04FE", instance.getTag(msgStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertEquals("04171F04FE", instance.getTag(msgConcentrator));
    }

    /**
     * Test of providesChecksum method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testProvidesChecksum() {
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertTrue( instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetCheckSum() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals("F6", instance.getCheckSum(msgStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertEquals("F6", instance.getCheckSum(msgConcentrator));
    }

    /**
     * Test of isValid method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testIsValid() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertTrue( instance.isValid(msgStandalone));
        assertFalse( instance.isValid(msgBadChkSumStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertTrue( instance.isValid(msgConcentrator));
        assertFalse( instance.isValid(msgBadChkSumConcentrator));
    }

    /**
     * Test of isCheckSumValid method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testIsCheckSumValid() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertTrue( instance.isCheckSumValid(msgStandalone));
        assertFalse( instance.isCheckSumValid(msgBadChkSumStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertTrue( instance.isCheckSumValid(msgConcentrator));
        assertFalse( instance.isCheckSumValid(msgBadChkSumConcentrator));

    }

    /**
     * Test of endOfMessage method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertTrue( instance.endOfMessage(msgStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertTrue( instance.endOfMessage(msgConcentrator));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetReaderPort() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        expResult = 'A';
        assertEquals(expResult, instance.getReaderPort(msgConcentrator));
    }

    /**
     * Test of toMonitorString method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testToMonitorString() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        String expResult = "Reply from CORE-ID reader. Tag read 04171F04FE checksum F6 valid? yes";
        assertEquals(expResult, instance.toMonitorString(msgStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        expResult = "Reply from CORE-ID reader. Reply from port A Tag read 04171F04FE checksum F6 valid? yes";
        assertEquals(expResult, instance.toMonitorString(msgConcentrator));
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
