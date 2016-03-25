// ReplyTest.java
package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.can.CanReply;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.lawicell.Reply class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class ReplyTest extends TestCase {

    // t123412345678
    public void testOne() {

        Reply g = new Reply("t123412345678\r");

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

    // T0000F00D0
    public void testTwo() {

        Reply g = new Reply("T0000F00D0\r");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        Assert.assertEquals("rtr", false, r.isRtr());
        Assert.assertEquals("header", 0xF00D, r.getHeader());
        Assert.assertEquals("num elements", 0, r.getNumDataElements());
    }

    public void testThree() {

        Reply g = new Reply("T00000123412345678\r");

        CanReply r = g.createReply();

        Assert.assertEquals("extended", true, r.isExtended());
        // not clear how to assert RTR in this protocol
        //Assert.assertEquals("rtr", true, r.isRtr());
        Assert.assertEquals("header", 0x123, r.getHeader());
        Assert.assertEquals("num elements", 4, r.getNumDataElements());
        Assert.assertEquals("el 0", 0x12, r.getElement(0));
        Assert.assertEquals("el 1", 0x34, r.getElement(1));
        Assert.assertEquals("el 2", 0x56, r.getElement(2));
        Assert.assertEquals("el 3", 0x78, r.getElement(3));
    }

    // from here down is testing infrastructure
    public ReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", ReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(ReplyTest.class);
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
