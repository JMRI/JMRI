package jmri.jmrix.tmcc;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SerialReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    SerialReply msg = null;

    public void testLength3() {
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        Assert.assertEquals("length ", 3, msg.getNumDataElements());
    }

    public void testLength1() {
        msg.setElement(0, 0x02);
        Assert.assertEquals("length ", 1, msg.getNumDataElements());
    }

    public void testToBinaryString() {
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0x12);
        Assert.assertEquals("string compare ", "81 02 12", msg.toString());
    }

    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0x12);
        Assert.assertEquals("string compare ", "81 02 12", msg.toString());
    }

    public void testToASCIIString() {
        msg.setOpCode(0x54);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        Assert.assertEquals("string compare ", "54 20 32", msg.toString());
    }

    @Before
    @Override
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
