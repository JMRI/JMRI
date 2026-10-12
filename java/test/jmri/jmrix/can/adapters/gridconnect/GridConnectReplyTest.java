package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @Test
    public void testExtendedEightDataBytes() {
        // longest valid frame, 28 characters including the ';'
        GridConnectReply g = new GridConnectReply(":X12345678N0102030405060708;");
        Assertions.assertEquals(28, g.getNumDataElements(), "28 characters");
        CanReply r = g.createReply();
        Assertions.assertTrue(r.isExtended(), "extended");
        Assertions.assertEquals(0x12345678, r.getHeader(), "header");
        Assertions.assertEquals(8, r.getNumDataElements(), "8 data bytes");
        for (int i = 0; i < 8; i++) {
            Assertions.assertEquals(i+1, r.getElement(i), "data byte " + i);
        }
        Assertions.assertEquals(28, g.maxSize(), "max size allows the ';' of the longest frame");
    }

    @Test
    public void testInvalidDataIsDropped() {
        String[] invalid = {
            ":S123N123;", // odd number of hex digits
            ":S123N12G4;", // not a hex digit
            ":S123N010203040506070809;", // 9 data bytes
            ":SB020N910001:SB020N9000;", // two frames merged
            ":SN12;", // no header digits
            ":S12345N12;", // 5 header digits in a standard frame
            ":X123456789N12;", // 9 header digits in an extended frame
            ":SB0G0N12;", // not a hex digit in the header
        };
        for (String s : invalid) {
            GridConnectReply g = new GridConnectReply(s);
            CanReply r = g.createReply();
            Assertions.assertEquals(0, r.getNumDataElements(), s + " dropped");
            jmri.util.JUnitAppender.assertWarnMessageStartsWith("Invalid GridConnect frame dropped");
        }
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new GridConnectReply(":S123N12345678;");
    }

    @AfterEach
    @Override
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }
}
