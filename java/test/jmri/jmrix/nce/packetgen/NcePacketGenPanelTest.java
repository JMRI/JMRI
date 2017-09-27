package jmri.jmrix.nce.packetgen;

import jmri.jmrix.nce.NceMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.nce.packetgen.NcePacketGenPanel class
 *
 * @author	Bob Jacobsen
 */
public class NcePacketGenPanelTest {

    @Test
    public void testPanelCreate() {
        new NcePacketGenPanel();
    }

    @Test
    public void testPacketNull() {
        NcePacketGenPanel t = new NcePacketGenPanel();
        NceMessage m = t.createPacket("");
        Assert.assertEquals("null pointer", null, m);
    }

    @Test
    public void testPacketCreate() {
        NcePacketGenPanel t = new NcePacketGenPanel();
        NceMessage m = t.createPacket("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x12, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x34, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x19, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x06, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, m.getElement(7) & 0xFF);
    }

    @Test
    public void testGetHelpTarget() {
        NcePacketGenPanel t = new NcePacketGenPanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.packetgen.NcePacketGenFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        NcePacketGenPanel t = new NcePacketGenPanel();
        Assert.assertEquals("title","NCE_: " + Bundle.getMessage("Title"),t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        NcePacketGenPanel t = new NcePacketGenPanel();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
