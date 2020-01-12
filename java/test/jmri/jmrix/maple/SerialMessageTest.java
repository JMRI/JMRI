package jmri.jmrix.maple;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2008
 */
public class SerialMessageTest extends jmri.jmrix.AbstractMessageTestBase {
        
    private SerialMessage msg = null;

    @Before
    @Override
    public void setUp() {
	JUnitUtil.setUp();
        m = msg = new SerialMessage(1);
    }

    @After
    public void tearDown(){
	m = msg = null;
	JUnitUtil.tearDown();
    }

    public void testToBinaryString() {
        msg = new SerialMessage(4);
        msg.setOpCode(0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0xA2);
        msg.setElement(3, 0x00);
        msg.setBinary(true);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    public void testBytesToString() {
        msg = new SerialMessage(4);
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        msg.setBinary(true);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    public void testToASCIIString() {
        msg = new SerialMessage(5);
        msg.setOpCode(0x54);
        msg.setElement(1, 0x20);
        msg.setElement(2, 0x32);
        msg.setElement(3, 0x84);
        msg.setElement(4, 0x05);
        msg.setBinary(false);
        Assert.assertEquals("string compare ", "54 20 32 84 05", msg.toString());
    }

}
