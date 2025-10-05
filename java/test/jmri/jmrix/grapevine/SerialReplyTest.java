package jmri.jmrix.grapevine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialReply class.
 *
 * @author Bob Jacobsen Copyright 2003, 2008
 */
public class SerialReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private SerialReply msg = null;

    @Test
    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        assertEquals( "81 02 A2 00", msg.toString(), "string compare ");
    }

    @Test
    public void testFormat1() {
        msg.setElement(0, (byte) 0x00);
        msg.setElement(1, (byte) 0x62);
        msg.setElement(2, (byte) 0x00);
        msg.setElement(3, (byte) 0x10);
        assertEquals( "Error report from node 98: Parity Error", msg.format(), "string compare ");
    }

    @Test
    public void testFormat2() {
        msg.setElement(0, (byte) 0xE2);
        msg.setElement(1, (byte) 0x06);
        msg.setNumDataElements(2);
        assertEquals( "Node 98 reports software version 6", msg.format(), "string compare ");
    }

    @Test
    public void testParallel() {
        msg.setElement(0, 128 + 98);
        msg.setElement(1, 0x0E);
        msg.setElement(2, 128 + 98);
        msg.setElement(3, 0x56);
        assertTrue( msg.isFromParallelSensor(), "parallel ");
        assertFalse( msg.isFromOldSerialSensor(), "old serial ");
        assertFalse( msg.isFromNewSerialSensor(), "new serial ");
    }

    @Test
    public void testOldSerial() {
        msg.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        msg.setElement(1, 0x6F);
        msg.setElement(2, 0x81);
        msg.setElement(3, 0x50);
        assertFalse( msg.isFromParallelSensor(), "parallel ");
        assertTrue( msg.isFromOldSerialSensor(), "old serial ");
        assertFalse( msg.isFromNewSerialSensor(), "new serial ");
    }

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       msg = new SerialReply();
       m = msg;
    }

    @Override
    @AfterEach
    public void tearDown(){
       m = null;
       msg = null;
       JUnitUtil.tearDown();
    }

}
