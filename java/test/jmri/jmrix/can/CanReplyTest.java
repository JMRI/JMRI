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
    public void testCreateArrayHeader() {
        CanReply m1 = new CanReply( new int[]{0x98, 0xDE, 0xFF, 0x23, 0x01},0x12 );
        Assert.assertNotNull("exists",m1);
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // Both CanReply and CanMessage are CanFrame with custom equals
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

        CanReply m4 = new CanReply(0x12);
        m4.setNumDataElements(1);
        m4.setElement(0, 0x07);
        
        Assert.assertTrue("equals self", m1.equals(m1));
        Assert.assertTrue("equals copy", m1.equals(new CanReply(m1)));
        Assert.assertTrue("equals same", m1.equals(m2));
        Assert.assertTrue("not equals diff Ext", !m1.equals(m3));
        Assert.assertTrue("not equals null", !m1.equals(null));
        Assert.assertTrue("not equals string value", !m1.equals("[12] 81 12"));
        Assert.assertTrue("not equals diff ele length", !m1.equals(m4));
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // Both CanReply and CanMessage are CanFrame with custom equals
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
    @SuppressWarnings("unlikely-arg-type") // Both CanReply and CanMessage are CanFrame with custom equals
    public void testReplyFromMessage() {
        CanMessage m = new CanMessage(0x555);
        m.setNumDataElements(2);
        m.setElement(0, 0x01);
        m.setElement(1, 0x82);
        Assert.assertEquals("2 Elements", 2,m.getNumDataElements());
        
        CanReply r = new CanReply(m);
        Assert.assertTrue("Header 0x555", r.getHeader() == 0x555);
        Assert.assertTrue("2 Elements", r.getNumDataElements() == 2);
        Assert.assertTrue("equals same", r.equals(m));
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
        
        msg.setNumDataElements(20);
        Assert.assertTrue("max 8 Elements", msg.getNumDataElements() == 8);
    }

    @Test
    @Override
    public void testToString() {
        msg.setHeader(0x12);
        msg.setNumDataElements(3);
        msg.setElement(0, 0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0x83);
        Assert.assertEquals("string representation", "[12] 81 02 83",msg.toString());
    }

    @Test
    @Override
    public void testToMonitorString() {
        msg.setHeader(0x12);
        msg.setNumDataElements(3);
        msg.setElement(0, 0x81);
        msg.setElement(1, 0x02);
        msg.setElement(2, 0x83);
        Assert.assertEquals("string representation", "(12) 81 02 83",msg.toMonitorString());
    }
    
    @Test
    public void testSkipPrefix() {
        Assert.assertTrue("skip prefix returns same", msg.skipPrefix(77) == 77);
    }
    
    @Test
    public void testCtorOverlength() {
        msg = new CanReply(new int[]{1,2,3,4,5,6,7,8,9});
        Assert.assertEquals("string representation", "(0) 01 02 03 04 05 06 07 08",msg.toMonitorString());
        
        msg = new CanReply(new int[]{1,2,3,4,5,6,7,8,9},7);
        Assert.assertEquals("string representation", "(7) 01 02 03 04 05 06 07 08",msg.toMonitorString());
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
