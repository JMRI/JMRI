package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficControllerScaffold;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.canrs.MergMessage class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class MergMessageTest extends TestCase {

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

        MergMessage g = new MergMessage(m);
        Assert.assertEquals("standard format 2 byte", ":S2460N12345678;", g.toString());
    }

    // :XF00DN;
    public void testTwo() {

        CanMessage m = new CanMessage(0xF00D);
        m.setExtended(true);
        m.setRtr(false);
        m.setNumDataElements(0);

        MergMessage g = new MergMessage(m);
        Assert.assertEquals("extended format 4 byte", ":X0008F00DN;", g.toString());
    }

    public void testThree() {

        CanMessage m = new CanMessage(0x12345678);
        m.setExtended(true);
        m.setRtr(true);
        m.setNumDataElements(4);
        m.setElement(0, 0x12);
        m.setElement(1, 0x34);
        m.setElement(2, 0x56);
        m.setElement(3, 0x78);

        MergMessage g = new MergMessage(m);
        Assert.assertEquals("extended format 4 byte", ":X91A85678R12345678;", g.toString());
    }

    // from here down is testing infrastructure
    public MergMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", MergMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MergMessageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        new TrafficControllerScaffold();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
