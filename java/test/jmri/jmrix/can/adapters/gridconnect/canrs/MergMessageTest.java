package jmri.jmrix.can.adapters.gridconnect.canrs;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @Test
    public void testByteOutOfRange() {
            g.setByte(-99, 0);
            JUnitAppender.assertErrorMessageStartsWith("Byte value -99 out of range 0-255 for MergMessage data payload");
            
            g.setByte(321, 0);
            JUnitAppender.assertErrorMessageStartsWith("Byte value 321 out of range 0-255 for MergMessage data payload");
            
            g.setByte(0xAA, 22);
            JUnitAppender.assertErrorMessageStartsWith("Byte Index 22 out of range 0-7 for MergMessage data payload");
            
            g.setByte(0xAA, -1);
            JUnitAppender.assertErrorMessageStartsWith("Byte Index -1 out of range 0-7 for MergMessage data payload");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

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

    @Override
    @AfterEach
    public void tearDown() {
        m = g = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
