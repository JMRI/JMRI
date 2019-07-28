package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialMessage;
import org.junit.Test;
import org.junit.Assert;

/**
 * JUnit tests for the cm11.SpecficMessage class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class SpecificMessageTest {

    @Test
    public void testCreate() {
        SerialMessage m = new SpecificMessage(4);
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testBytesToString() {
        SerialMessage m = new SpecificMessage(4);
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

}
