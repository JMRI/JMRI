package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.CanReply;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.canrs.MergReply class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class MergReplyTest extends TestCase {

    // :S1260N12345678;
    public void testOne() {

        MergReply g = new MergReply(":S1260N12345678;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", false, r.isExtended());
        Assert.assertEquals("rtr", false, r.isRtr());
        Assert.assertEquals("header", unMungeStdHeader(0x1260), r.getHeader());
        Assert.assertEquals("num elements", 4, r.getNumDataElements());
        Assert.assertEquals("el 0", 0x12, r.getElement(0));
        Assert.assertEquals("el 1", 0x34, r.getElement(1));
        Assert.assertEquals("el 2", 0x56, r.getElement(2));
        Assert.assertEquals("el 3", 0x78, r.getElement(3));
    }

    // :XF00DN;
    public void testTwo() {

        MergReply g = new MergReply(":XF00DN;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", false, r.isRtr());
        Assert.assertEquals("header", 0xF00D, r.getHeader());
        Assert.assertEquals("num elements", 0, r.getNumDataElements());
    }

    public void testThree() {

        MergReply g = new MergReply(":X123R12345678;");

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

    public void testThreeAlt() {

        MergReply g = new MergReply(":X0000123R12345678;");

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

    public void testThreeBis() {

        MergReply g = new MergReply(":X000123R12345678;");

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

    public void testFour() {

        MergReply g = new MergReply(":XFFE3FFFFR63;");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", true, r.isRtr());
        Assert.assertEquals("header", unMungeExtHeader(0xFFE3FFFF), r.getHeader());
        Assert.assertEquals("num elements", 1, r.getNumDataElements());
        Assert.assertEquals("el 1", 0x63, r.getElement(0));
    }

    // from here down is testing infrastructure
    public MergReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", MergReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MergReplyTest.class);
        return suite;
    }

    // Left shift a standard header from CBUS specific format
    public int unMungeStdHeader(int h) {
        return (h >> 5);
    }

    // Un-munge extended header from CBUS specific format
    public int unMungeExtHeader(int h) {
        return (((h >> 3) & 0x1FFC0000) | (h & 0x3FFFF));
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
