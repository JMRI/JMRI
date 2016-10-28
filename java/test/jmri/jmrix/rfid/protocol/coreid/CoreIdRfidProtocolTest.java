package jmri.jmrix.rfid.protocol.coreid;

import jmri.jmrix.AbstractMRReply;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the CoreIdRfidProtocol class
 *
 * @author Matthew Harris
 */
public class CoreIdRfidProtocolTest extends TestCase {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\u000204171F04FEF6\r\n\u0003");
    AbstractMRReply msgConcentrator = new AbstractMRReplyImpl("A04171F04FEF6\r\n>");
    AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("\u000204171F04FEF7\r\n\u0003");
    AbstractMRReply msgBadChkSumConcentrator = new AbstractMRReplyImpl("A04171F04FEF7\r\n>");

    /**
     * Test of getMaxSize method, of class CoreIdRfidProtocol.
     */
    public void testGetMaxSize() {
        assertEquals(16, CoreIdRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class CoreIdRfidProtocol.
     */
    public void testInitString() {
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class CoreIdRfidProtocol.
     */
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
    public void testProvidesChecksum() {
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals(true, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class CoreIdRfidProtocol.
     */
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
    public void testIsValid() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgBadChkSumStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertEquals(true, instance.isValid(msgConcentrator));
        assertEquals(false, instance.isValid(msgBadChkSumConcentrator));
    }

    /**
     * Test of isCheckSumValid method, of class CoreIdRfidProtocol.
     */
    public void testIsCheckSumValid() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals(true, instance.isCheckSumValid(msgStandalone));
        assertEquals(false, instance.isCheckSumValid(msgBadChkSumStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertEquals(true, instance.isCheckSumValid(msgConcentrator));
        assertEquals(false, instance.isCheckSumValid(msgBadChkSumConcentrator));

    }

    /**
     * Test of endOfMessage method, of class CoreIdRfidProtocol.
     */
    public void testEndOfMessage() {
        // First as stand-alone
        CoreIdRfidProtocol instance = new CoreIdRfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
        // Now as concentrator
        instance = new CoreIdRfidProtocol('A', 'H', 1);
        assertEquals(true, instance.endOfMessage(msgConcentrator));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
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
    public CoreIdRfidProtocolTest(String testName) {
        super(testName);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", CoreIdRfidProtocolTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CoreIdRfidProtocolTest.class);
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
