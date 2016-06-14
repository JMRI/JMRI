package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.CanMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.GridConnectMessage class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class GridConnectMessageTest extends TestCase {

    // :S123N12345678;
    public void testOne() {

        CanMessage m = new CanMessage(0x123);
        m.setExtended(false);
        m.setRtr(false);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        GridConnectMessage g = new GridConnectMessage(m);
        Assert.assertEquals("standard format 2 byte", ":S123N12345678;", g.toString());
    }

    // :XF00DN;
    public void testTwo() {

        CanMessage m = new CanMessage(0xF00D);
        m.setExtended(true);
        m.setRtr(false);
        m.setNumDataElements(0);

        GridConnectMessage g = new GridConnectMessage(m);
        Assert.assertEquals("standard format 2 byte", ":X0000F00DN;", g.toString());
    }

    public void testThree() {

        CanMessage m = new CanMessage(0x123);
        m.setExtended(true);
        m.setRtr(true);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        GridConnectMessage g = new GridConnectMessage(m);
        Assert.assertEquals("standard format 2 byte", ":X00000123R12345678;", g.toString());
    }

    // from here down is testing infrastructure
    public GridConnectMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", GridConnectMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(GridConnectMessageTest.class);
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
