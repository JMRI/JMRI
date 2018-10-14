package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.adapters.lawicell.Message class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class MessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private Message g = null;

    // t123412345678
    @Test
    public void testOne() {
        Assert.assertEquals("standard format 2 byte", "t123412345678\r", g.toString());
    }

    // T0000F00D0
    @Test
    public void testTwo() {

        CanMessage msg = new CanMessage(0xF00D);
        msg.setExtended(true);
        msg.setRtr(false);
        msg.setNumDataElements(0);

        Message g = new Message(msg);
        Assert.assertEquals("standard format 2 byte", "T0000F00D0\r", g.toString());
    }

    @Test
    public void testThree() {

        CanMessage msg = new CanMessage(0x123);
        msg.setExtended(true);
        // not clear how to set RTR in this protocol
        //msg.setRtr(true);
        msg.setNumDataElements(4);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x56);
        msg.setElement(3, 0x78);

        Message g = new Message(msg);
        Assert.assertEquals("standard format 2 byte", "T00000123412345678\r", g.toString());
    }

    // T0000F00D0
    @Test
    public void testFour() {

        CanMessage msg = new CanMessage(0xF00D);
        msg.setExtended(true);
        msg.setRtr(false);
        msg.setNumDataElements(8);
        msg.setElement(0, 0x78);
        msg.setElement(1, 0x78);
        msg.setElement(2, 0x78);
        msg.setElement(3, 0x78);
        msg.setElement(4, 0x78);
        msg.setElement(5, 0x78);
        msg.setElement(6, 0x78);
        msg.setElement(7, 0x78);

        Message g = new Message(msg);
        Assert.assertEquals("standard format 2 byte", "T0000F00D87878787878787878\r", g.toString());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

	CanMessage msg = new CanMessage(0x123);
        msg.setExtended(false);
        msg.setRtr(false);
        msg.setNumDataElements(4);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x56);
        msg.setElement(3, 0x78);

        m = g = new Message(msg);
    }

    @After
    public void tearDown() {
	m = g = null;
        JUnitUtil.tearDown();
    }
}
