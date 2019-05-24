package jmri.jmrix.tmcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SerialMessageTest extends jmri.jmrix.AbstractMessageTestBase {
        
    SerialMessage msg = null;

    @Test
    public void testToBinaryString() {
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        Assert.assertEquals("string compare ", "81 02 A2", msg.toString());
    }

    @Test
    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        Assert.assertEquals("string compare ", "81 02 A2", msg.toString());
    }

    @Test
    public void testToASCIIString() {
        msg.setOpCode(0x54);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        Assert.assertEquals("string compare ", "54 20 32", msg.toString());
    }

    @Override
    @Before
    public void setUp(){
	JUnitUtil.setUp();
        m = msg = new SerialMessage(1);
    }

    @After
    public void tearDown(){
	m = msg = null;
	JUnitUtil.tearDown();
    }

}
