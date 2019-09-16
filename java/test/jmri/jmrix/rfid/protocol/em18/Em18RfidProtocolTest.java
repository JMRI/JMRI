package jmri.jmrix.rfid.protocol.em18;

import jmri.jmrix.AbstractMRReply;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the Em18RfidProtocol class
 *
 * @author Matthew Harris
 */
public class Em18RfidProtocolTest {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("7800656EB6C5");
    AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("7800656EB6C6");

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
        assertEquals(true, instance.providesChecksum());
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
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgBadChkSumStandalone));
    }

    /**
     * Test of isCheckSumValid method, of class Em18RfidProtocol.
     */
    @Test
    public void testIsCheckSumValid() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals(true, instance.isCheckSumValid(msgStandalone));
        assertEquals(false, instance.isCheckSumValid(msgBadChkSumStandalone));
    }

    /**
     * Test of endOfMessage method, of class Em18RfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
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
