package jmri.jmrix.rfid.protocol.parallax;

import jmri.jmrix.AbstractMRReply;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the ParallaxRfidProtocol class
 *
 * @author Matthew Harris
 */
public class ParallaxRfidProtocolTest extends TestCase {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\n7800656EB6\r");
    AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n7800656EB6");

    /**
     * Test of getMaxSize method, of class ParallaxRfidProtocol.
     */
    public void testGetMaxSize() {
        assertEquals(12, ParallaxRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class ParallaxRfidProtocol.
     */
    public void testInitString() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class ParallaxRfidProtocol.
     */
    public void testGetTag() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals("7800656EB6", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class ParallaxRfidProtocol.
     */
    public void testProvidesChecksum() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals(false, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class ParallaxRfidProtocol.
     */
    public void testGetCheckSum() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals("", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class ParallaxRfidProtocol.
     */
    public void testIsValid() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class ParallaxRfidProtocol.
     */
    public void testEndOfMessage() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    public void testGetReaderPort() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class ParallaxRfidProtocol.
     */
    public void testToMonitorString() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        String expResult = "Reply from Parallax reader. Tag read 7800656EB6";
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
    public ParallaxRfidProtocolTest(String testName) {
        super(testName);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ParallaxRfidProtocolTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ParallaxRfidProtocolTest.class);
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

    private final static Logger log = LoggerFactory.getLogger(ParallaxRfidProtocolTest.class.getName());

}
