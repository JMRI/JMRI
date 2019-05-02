package jmri.jmrix.rfid.protocol.parallax;

import jmri.jmrix.AbstractMRReply;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for the ParallaxRfidProtocol class
 *
 * @author Matthew Harris
 */
public class ParallaxRfidProtocolTest {

    AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\n7800656EB6\r");
    AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n7800656EB6");

    /**
     * Test of getMaxSize method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testGetMaxSize() {
        assertEquals(12, ParallaxRfidProtocol.getMaxSize());
    }

    /**
     * Test of initString method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testInitString() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals("", instance.initString());
    }

    /**
     * Test of getTag method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testGetTag() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals("7800656EB6", instance.getTag(msgStandalone));
    }

    /**
     * Test of providesChecksum method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testProvidesChecksum() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals(false, instance.providesChecksum());
    }

    /**
     * Test of getCheckSum method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testGetCheckSum() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals("", instance.getCheckSum(msgStandalone));
    }

    /**
     * Test of isValid method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testIsValid() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals(true, instance.isValid(msgStandalone));
        assertEquals(false, instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertEquals(true, instance.endOfMessage(msgStandalone));
    }

    /**
     * Test of getReaderPort method, of class CoreIdRfidProtocol.
     */
    @Test
    public void testGetReaderPort() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        char expResult = 0x00;
        assertEquals(expResult, instance.getReaderPort(msgStandalone));
    }

    /**
     * Test of toMonitorString method, of class ParallaxRfidProtocol.
     */
    @Test
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

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

}
