package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.adapters.gridconnect.canrs.MergMessage class
 *
 * @author Bob Jacobsen Copyright 2008, 2009
 */
public class MergMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private MergMessage g = null;

    // :S123N12345678;
    @Test
    public void testOne() {
        Assert.assertEquals("standard format 2 byte", ":S2460N12345678;", g.toString());
    }

    // :XF00DN;
    @Test
    public void testTwo() {

        CanMessage msg = new CanMessage(0xF00D);
        msg.setExtended(true);
        msg.setRtr(false);
        msg.setNumDataElements(0);

        g = new MergMessage(msg);
        Assert.assertEquals("extended format 4 byte", ":X0008F00DN;", g.toString());
    }

    @Test
    public void testThree() {

        CanMessage msg = new CanMessage(0x12345678);
        msg.setExtended(true);
        msg.setRtr(true);
        msg.setNumDataElements(4);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x56);
        msg.setElement(3, 0x78);

        g = new MergMessage(msg);
        Assert.assertEquals("extended format 4 byte", ":X91A85678R12345678;", g.toString());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        new TrafficControllerScaffold();
        CanMessage msg = new CanMessage(0x123);
        msg.setExtended(false);
        msg.setRtr(false);
        msg.setNumDataElements(4);
        msg.setElement(0, 0x12);
        msg.setElement(1, 0x34);
        msg.setElement(2, 0x56);
        msg.setElement(3, 0x78);

        m = g = new MergMessage(msg);
    }

    @After
    public void tearDown() {
	m = g = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
