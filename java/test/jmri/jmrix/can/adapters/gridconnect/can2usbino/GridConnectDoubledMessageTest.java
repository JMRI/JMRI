package jmri.jmrix.can.adapters.gridconnect.can2usbino;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.GridConnectDoubledMessage class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class GridConnectDoubledMessageTest {

    @Test
    // !S123N12345678;
    public void testOne() {

        CanMessage m = new CanMessage(0x123);
        m.setExtended(false);
        m.setRtr(false);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        GridConnectDoubledMessage g = new GridConnectDoubledMessage(m);
        Assert.assertEquals("standard format 2 byte", "!S123N12345678;", g.toString());
    }

    @Test
    // !XF00DN;
    public void testTwo() {

        CanMessage m = new CanMessage(0xF00D);
        m.setExtended(true);
        m.setRtr(false);
        m.setNumDataElements(0);

        GridConnectDoubledMessage g = new GridConnectDoubledMessage(m);
        Assert.assertEquals("standard format 2 byte", "!X0000F00DN;", g.toString());
    }

    @Test
    public void testThree() {

        CanMessage m = new CanMessage(0x123);
        m.setExtended(true);
        m.setRtr(true);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        GridConnectDoubledMessage g = new GridConnectDoubledMessage(m);
        Assert.assertEquals("standard format 2 byte", "!X00000123R12345678;", g.toString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
