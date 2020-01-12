package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the SerialMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class SerialMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private SerialMessage msg = null;

    @Before
    @Override
    public void setUp() {
	JUnitUtil.setUp();
        m = msg = new SerialMessage(5);
    }

    @After
    public void tearDown(){
	m = msg = null;
	JUnitUtil.tearDown();
    }

    public void testBytesToString() {
        msg = new SerialMessage(5);
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00 21", msg.toString());
    }

}
