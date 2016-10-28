package jmri.jmrix.rfid.protocol.em18;

import jmri.jmrix.AbstractMRReply;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Em18RfidProtocol class
 *
 * @author Matthew Harris
 */
public class Em18RfidProtocolTest extends TestCase {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("7800656EB6C5");
    AbstractMRReply msgBadChkSumStandalone = new AbstractMRReplyImpl("7800656EB6C6");

    /**
     * Test of getMaxSize method, of class Em18RfidProtocol.
     */
    public void testGetMaxSize() {
        assertEquals(12, Em18RfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class Em18RfidProtocol.
     */
    public void testInitString() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class Em18RfidProtocol.
     */
    public void testGetTag() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals("7800656EB6", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class Em18RfidProtocol.
     */
    public void testProvidesChecksum() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals(true, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class Em18RfidProtocol.
     */
    public void testGetCheckSum() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals("C5", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class Em18RfidProtocol.
     */
    public void testIsValid() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgBadChkSumStandalone));
    }

    /**
     * Test of isCheckSumValid method, of class Em18RfidProtocol.
     */
    public void testIsCheckSumValid() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals(true, instance.isCheckSumValid(msgStandalone));
        assertEquals(false, instance.isCheckSumValid(msgBadChkSumStandalone));
    }

    /**
     * Test of endOfMessage method, of class Em18RfidProtocol.
     */
    public void testEndOfMessage() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    public void testGetReaderPort() {
        Em18RfidProtocol instance = new Em18RfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class Em18RfidProtocol.
     */
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

    // from here down is testing infrastructure
    public Em18RfidProtocolTest(String testName) {
        super(testName);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", Em18RfidProtocolTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Em18RfidProtocolTest.class);
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
