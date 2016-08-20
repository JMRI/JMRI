package jmri.jmrix.can;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.CanReply class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class CanReplyTest extends CanMRCommonTest {

    public void testCopyCtor() {
        CanReply m1 = new CanReply();
        m1.setExtended(true);
        m1.setHeader(0x12);

        CanReply m2 = new CanReply(m1);
        Assert.assertTrue("extended", m2.isExtended());
        Assert.assertTrue("header", m2.getHeader() == 0x12);
    }

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

    public void testHeaderAccessors() {
        CanReply m = new CanReply();

        m.setHeader(0x555);
        Assert.assertTrue("Header 0x555", m.getHeader() == 0x555);

    }

    public void testRtrBit() {
        CanReply m = new CanReply();
        Assert.assertTrue("not rtr at start", !m.isRtr());
        m.setRtr(true);
        Assert.assertTrue("rtr set", m.isRtr());
        m.setRtr(false);
        Assert.assertTrue("rtr unset", !m.isRtr());
    }

    public void testStdExt() {
        CanReply m = new CanReply();
        Assert.assertTrue("std at start", !m.isExtended());
        m.setExtended(true);
        Assert.assertTrue("extended", m.isExtended());
        m.setExtended(false);
        Assert.assertTrue("std at end", !m.isExtended());
    }

    public void testDataElements() {
        CanReply m = new CanReply();

        m.setNumDataElements(0);
        Assert.assertTrue("0 Elements", m.getNumDataElements() == 0);

        m.setNumDataElements(1);
        Assert.assertTrue("1 Elements", m.getNumDataElements() == 1);

        m.setNumDataElements(8);
        Assert.assertTrue("8 Elements", m.getNumDataElements() == 8);

        m.setNumDataElements(3);
        m.setElement(0, 0x81);
        m.setElement(1, 0x02);
        m.setElement(2, 0x83);
        Assert.assertTrue("3 Elements", m.getNumDataElements() == 3);
        Assert.assertTrue("3 Element 0", m.getElement(0) == 0x81);
        Assert.assertTrue("3 Element 1", m.getElement(1) == 0x02);
        Assert.assertTrue("3 Element 2", m.getElement(2) == 0x83);
    }

    // from here down is testing infrastructure
    public CanReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", CanReplyTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(CanReplyTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
