// MessageTest.java
package jmri.jmrix.can.adapters.lawicell;

import jmri.jmrix.can.CanMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.lawicell.Message class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class MessageTest extends TestCase {

    // t123412345678
    public void testOne() {

        CanMessage m = new CanMessage(0x123);
        m.setExtended(false);
        m.setRtr(false);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        Message g = new Message(m);
        Assert.assertEquals("standard format 2 byte", "t123412345678\r", g.toString());
    }

    // T0000F00D0
    public void testTwo() {

        CanMessage m = new CanMessage(0xF00D);
        m.setExtended(true);
        m.setRtr(false);
        m.setNumDataElements(0);

        Message g = new Message(m);
        Assert.assertEquals("standard format 2 byte", "T0000F00D0\r", g.toString());
    }

    public void testThree() {

        CanMessage m = new CanMessage(0x123);
        m.setExtended(true);
        // not clear how to set RTR in this protocol
        //m.setRtr(true);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        Message g = new Message(m);
        Assert.assertEquals("standard format 2 byte", "T00000123412345678\r", g.toString());
    }

    // T0000F00D0
    public void testFour() {

        CanMessage m = new CanMessage(0xF00D);
        m.setExtended(true);
        m.setRtr(false);
        m.setNumDataElements(8);
        m.setElement(0, 0x78);
        m.setElement(1, 0x78);
        m.setElement(2, 0x78);
        m.setElement(3, 0x78);
        m.setElement(4, 0x78);
        m.setElement(5, 0x78);
        m.setElement(6, 0x78);
        m.setElement(7, 0x78);

        Message g = new Message(m);
        Assert.assertEquals("standard format 2 byte", "T0000F00D87878787878787878\r", g.toString());
    }
    // from here down is testing infrastructure

    public MessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", MessageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MessageTest.class);
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
