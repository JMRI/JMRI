package jmri.jmrix.powerline.simulator;

import jmri.jmrix.powerline.SerialMessage;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the cm11.SpecficMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 */
public class SpecificMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private SerialMessage msg;

    @Before
    @Override
    public void setUp() {
	JUnitUtil.setUp();
        m = msg = new SpecificMessage(4);
    }

    @After
    public void tearDown(){
	m = msg = null;
	JUnitUtil.tearDown();
    }

    @Test
    public void testBytesToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

}
