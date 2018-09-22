package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.GridConnectMessage class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class GridConnectMessageTest extends jmri.jmrix.AbstractMessageTestBase {
        
    private GridConnectMessage g = null;

    // :S123N12345678;
    @Test
    public void testOne() {
        Assert.assertEquals("standard format 2 byte", ":S123N12345678;", g.toString());
    }

    // :XF00DN;
    @Test
    public void testTwo() {

        CanMessage msg = new CanMessage(0xF00D);
        msg.setExtended(true);
        msg.setRtr(false);
        msg.setNumDataElements(0);

        g = new GridConnectMessage(msg);
        Assert.assertEquals("standard format 2 byte", ":X0000F00DN;", g.toString());
    }

    @Test
    public void testThree() {

        CanMessage msg = new CanMessage(0x123);
        msg.setExtended(true);
        msg.setRtr(true);
        msg.setNumDataElements(4);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x56);
        msg.setElement(3, 0x78);

        g = new GridConnectMessage(msg);
        Assert.assertEquals("standard format 2 byte", ":X00000123R12345678;", g.toString());
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

        m = g = new GridConnectMessage(msg);
    }

    @After
    public void tearDown() {
	m = g = null;
        JUnitUtil.tearDown();
    }
}
