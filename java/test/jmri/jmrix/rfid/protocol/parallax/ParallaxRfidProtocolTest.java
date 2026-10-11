package jmri.jmrix.rfid.protocol.parallax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMRReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the ParallaxRfidProtocol class
 *
 * @author Matthew Harris
 */
public class ParallaxRfidProtocolTest {

    private final AbstractMRReply msgStandalone = new AbstractMRReplyImpl("\n7800656EB6\r");
    private final AbstractMRReply msgInvalidStandalone = new AbstractMRReplyImpl("\r\n7800656EB6");

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
        assertFalse( instance.providesChecksum());
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
        assertTrue( instance.isValid(msgStandalone));
        assertFalse( instance.isValid(msgInvalidStandalone));
    }

    /**
     * Test of endOfMessage method, of class ParallaxRfidProtocol.
     */
    @Test
    public void testEndOfMessage() {
        ParallaxRfidProtocol instance = new ParallaxRfidProtocol();
        assertTrue( instance.endOfMessage(msgStandalone));
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
