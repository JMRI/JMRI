package jmri.jmrix.can;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.CanReply class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class CanReplyTest extends CanMRCommonTestBase {

    private CanReply msg = null;

    @Test
    public void testCopyCtor() {
        CanReply m1 = new CanReply();
        m1.setExtended(true);
        m1.setHeader(0x12);

        CanReply m2 = new CanReply(m1);
        Assert.assertTrue("extended", m2.isExtended());
        Assert.assertTrue("header", m2.getHeader() == 0x12);
    }

    @Test
    public void testEqualsOp() {
        CanReply m1 = new CanReply();
        m1.setExtended(true);
        m1.setHeader(0x12);

        CanReply m2 = new CanReply();
        m2.setExtended(true);
        m2.setHeader(0x12);

        CanReply m3 = new CanReply();
        m3.setExtended(false);
        m3.setHeader(0x12);

        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanReply(m1)));
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("not equals diff Ext", !m1.equals(m3));
    }

    @Test
    public void testEqualsMessage() {
        CanReply m1 = new CanReply();
        m1.setExtended(true);
        m1.setHeader(0x12);
        m1.setNumDataElements(0);

        CanMessage m2 = new CanMessage(0, 0x12);
        m2.setExtended(true);
        m2.setNumDataElements(0);

        CanMessage m3 = new CanMessage(0x12);
        m3.setExtended(false);
        m3.setNumDataElements(0);

        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("not equals diff Ext", !m1.equals(m3));
    }

    @Test
    public void testEqualsData() {
        CanReply m1 = new CanReply();
        m1.setNumDataElements(2);
        m1.setElement(0, 0x81);
        m1.setElement(1, 0x12);

        CanReply m2 = new CanReply();
        m2.setNumDataElements(2);
        m2.setElement(0, 0x81);
        m2.setElement(1, 0x12);

        CanReply m3 = new CanReply();
        m3.setNumDataElements(2);
        m3.setElement(0, 0x01);
        m3.setElement(1, 0x82);

        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanReply(m1)));
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("not equals diff Ext", !m1.equals(m3));
    }

    @Test
    public void testHeaderAccessors() {
        msg.setHeader(0x555);
        Assert.assertTrue("Header 0x555", msg.getHeader() == 0x555);

    }

    @Test
    public void testRtrBit() {
        Assert.assertTrue("not rtr at start", !msg.isRtr());
        msg.setRtr(true);
        Assert.assertTrue("rtr set", msg.isRtr());
        msg.setRtr(false);
        Assert.assertTrue("rtr unset", !msg.isRtr());
    }

    @Test
    public void testStdExt() {
        Assert.assertTrue("std at start", !msg.isExtended());
        msg.setExtended(true);
        Assert.assertTrue("extended", msg.isExtended());
        msg.setExtended(false);
        Assert.assertTrue("std at end", !msg.isExtended());
    }

    @Test
    public void testDataElements() {

        msg.setNumDataElements(0);
        Assert.assertTrue("0 Elements", msg.getNumDataElements() == 0);

        msg.setNumDataElements(1);
        Assert.assertTrue("1 Elements", msg.getNumDataElements() == 1);

        msg.setNumDataElements(8);
        Assert.assertTrue("8 Elements", msg.getNumDataElements() == 8);

        msg.setNumDataElements(3);
        msg.setElement(0, 0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0x83);
        Assert.assertTrue("3 Elements", msg.getNumDataElements() == 3);
        Assert.assertTrue("3 Element 0", msg.getElement(0) == 0x81);
        Assert.assertTrue("3 Element 1", msg.getElement(1) == 0x02);
        Assert.assertTrue("3 Element 2", msg.getElement(2) == 0x83);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new CanReply();
    }

    @Override
    @After
    public void tearDown() {
	m = msg = null;
        JUnitUtil.tearDown();
    }
}
