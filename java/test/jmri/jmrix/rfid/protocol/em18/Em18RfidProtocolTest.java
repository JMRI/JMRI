package jmri.jmrix.rfid.protocol.em18;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMRReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the Em18RfidProtocol class
 *
 * @author Matthew Harris
 */
public class Em18RfidProtocolTest {

    private final AbstractMRReply msgStandalone = new AbstractMRReplyImpl("7800656EB6C5");
    private final AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("7800656EB6C6");

    /**
     * Test of getMaxSize method, of class Em18RfidProtocol.
     */
    @Test
    public void testGetMaxSize() {
        assertEquals(12, Em18RfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class Em18RfidProtocol.
     */
    @Test
    public void testInitString() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class Em18RfidProtocol.
     */
    @Test
    public void testGetTag() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals("7800656EB6", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class Em18RfidProtocol.
     */
    @Test
    public void testProvidesChecksum() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertTrue( instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class Em18RfidProtocol.
     */
    @Test
    public void testGetCheckSum() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals("C5", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class Em18RfidProtocol.
     */
    @Test
    public void testIsValid() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertTrue( instance.isValid(msgStandalone));
        assertFalse( instance.isValid(msgBadChkSumStandalone));
    }

    /**
     * Test of isCheckSumValid method, of class Em18RfidProtocol.
     */
    @Test
    public void testIsCheckSumValid() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertTrue( instance.isCheckSumValid(msgStandalone));
        assertFalse( instance.isCheckSumValid(msgBadChkSumStandalone));
    }

    /**
     * Test of endOfMessage method, of class Em18RfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertTrue( instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetReaderPort() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class Em18RfidProtocol.
     */
    @Test
    public void testToMonitorString() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        String expResult = "Reply from EM-18 reader. Tag read 7800656EB6 checksum C5 valid? yes";
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
