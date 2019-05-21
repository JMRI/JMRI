package jmri.jmrix.grapevine;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialReply class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2008
 */
public class SerialReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    private SerialReply msg = null;

    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    public void testFormat1() {
        msg.setElement(0, (byte) 0x00);
        msg.setElement(1, (byte) 0x62);
        msg.setElement(2, (byte) 0x00);
        msg.setElement(3, (byte) 0x10);
        Assert.assertEquals("string compare ", "Error report from node 98: Parity Error", msg.format());
    }

    public void testFormat2() {
        msg.setElement(0, (byte) 0xE2);
        msg.setElement(1, (byte) 0x06);
        msg.setNumDataElements(2);
        Assert.assertEquals("string compare ", "Node 98 reports software version 6", msg.format());
    }

    public void testParallel() {
        msg.setElement(0, 128 + 98);
        msg.setElement(1, 0x0E);
        msg.setElement(2, 128 + 98);
        msg.setElement(3, 0x56);
        Assert.assertEquals("parallel ", true, msg.isFromParallelSensor());
        Assert.assertEquals("old serial ", false, msg.isFromOldSerialSensor());
        Assert.assertEquals("new serial ", false, msg.isFromNewSerialSensor());
    }

    public void testOldSerial() {
        msg.setElement(0, 0x81); // sensor 1-4 (from 0) inactive
        msg.setElement(1, 0x6F);
        msg.setElement(2, 0x81);
        msg.setElement(3, 0x50);
        Assert.assertEquals("parallel ", false, msg.isFromParallelSensor());
        Assert.assertEquals("old serial ", true, msg.isFromOldSerialSensor());
        Assert.assertEquals("new serial ", false, msg.isFromNewSerialSensor());
    }

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       m = msg = new SerialReply();
    }

    @After
    public void tearDown(){
       m = msg = null;
       JUnitUtil.tearDown();
    }

}
