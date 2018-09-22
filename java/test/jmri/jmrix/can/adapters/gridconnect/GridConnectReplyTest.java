package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.GridConnectReply class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class GridConnectReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    // :S123N12345678;
    @Test
    public void testOne() {

        GridConnectReply g = new GridConnectReply(":S123N12345678;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", false, r.isExtended());
        Assert.assertEquals("rtr", false, r.isRtr());
        Assert.assertEquals("header", 0x123, r.getHeader());
        Assert.assertEquals("num elements", 4, r.getNumDataElements());
        Assert.assertEquals("el 0", 0x12, r.getElement(0));
        Assert.assertEquals("el 1", 0x34, r.getElement(1));
        Assert.assertEquals("el 2", 0x56, r.getElement(2));
        Assert.assertEquals("el 3", 0x78, r.getElement(3));
    }

    // :XF00DN;
    @Test
    public void testTwo() {

        GridConnectReply g = new GridConnectReply(":XF00DN;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", false, r.isRtr());
        Assert.assertEquals("header", 0xF00D, r.getHeader());
        Assert.assertEquals("num elements", 0, r.getNumDataElements());
    }

    @Test
    public void testThree() {

        GridConnectReply g = new GridConnectReply(":X123R12345678;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", true, r.isRtr());
        Assert.assertEquals("header", 0x123, r.getHeader());
        Assert.assertEquals("num elements", 4, r.getNumDataElements());
        Assert.assertEquals("el 0", 0x12, r.getElement(0));
        Assert.assertEquals("el 1", 0x34, r.getElement(1));
        Assert.assertEquals("el 2", 0x56, r.getElement(2));
        Assert.assertEquals("el 3", 0x78, r.getElement(3));
    }

    @Test
    public void testThreeAlt() {

        GridConnectReply g = new GridConnectReply(":X0000123R12345678;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", true, r.isRtr());
        Assert.assertEquals("header", 0x123, r.getHeader());
        Assert.assertEquals("num elements", 4, r.getNumDataElements());
        Assert.assertEquals("el 0", 0x12, r.getElement(0));
        Assert.assertEquals("el 1", 0x34, r.getElement(1));
        Assert.assertEquals("el 2", 0x56, r.getElement(2));
        Assert.assertEquals("el 3", 0x78, r.getElement(3));
    }

    @Test
    public void testThreeBis() {

        GridConnectReply g = new GridConnectReply(":X000123R12345678;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", true, r.isRtr());
        Assert.assertEquals("header", 0x123, r.getHeader());
        Assert.assertEquals("num elements", 4, r.getNumDataElements());
        Assert.assertEquals("el 0", 0x12, r.getElement(0));
        Assert.assertEquals("el 1", 0x34, r.getElement(1));
        Assert.assertEquals("el 2", 0x56, r.getElement(2));
        Assert.assertEquals("el 3", 0x78, r.getElement(3));
    }

    @Test
    public void testFour() {

        GridConnectReply g = new GridConnectReply(":X1FFFFFFFR63;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", true, r.isRtr());
        Assert.assertEquals("header", 0x1FFFFFFF, r.getHeader());
        Assert.assertEquals("num elements", 1, r.getNumDataElements());
        Assert.assertEquals("el 1", 0x63, r.getElement(0));
    }

    @Test
    public void testNotNegative() {

        // remnant of Arduino CAN2USBino startup message
        GridConnectReply g = new GridConnectReply(": 1;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", false, r.isExtended());
        Assert.assertEquals("rtr", false, r.isRtr());
        Assert.assertEquals("header", 0x0, r.getHeader());
        Assert.assertEquals("num elements", 0, r.getNumDataElements());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new GridConnectReply(":S123N12345678;");
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }
}
