package jmri.jmrix.tmcc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class SerialReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private SerialReply msg = null;

    @Test
    public void testLength3() {
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        assertEquals( 3, msg.getNumDataElements(), "length ");
    }

    @Test
    public void testLength1() {
        msg.setElement(0, 0x02);
        assertEquals( 1, msg.getNumDataElements(), "length ");
    }

    @Test
    public void testToBinaryString() {
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0x12);
        assertEquals( "81 02 12", msg.toString(), "string compare ");
    }

    @Test
    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0x12);
        assertEquals( "81 02 12", msg.toString(), "string compare ");
    }

    @Test
    public void testToASCIIString() {
        msg.setOpCode(0x54);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        assertEquals( "54 20 32", msg.toString(), "string compare ");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        msg = new SerialReply();
        m = msg;
    }

    @Override
    @AfterEach
    public void tearDown() {
        m = null;
        msg = null;
        JUnitUtil.tearDown();
    }

}
