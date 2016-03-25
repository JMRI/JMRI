// NcePacketGenPanelTest.java
package jmri.jmrix.nce.packetgen;

import jmri.jmrix.nce.NceMessage;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.nce.packetgen.NcePacketGenPanel class
 *
 * @author	Bob Jacobsen
 */
public class NcePacketGenPanelTest extends TestCase {

    public void testPanelCreate() {
        new NcePacketGenPanel();
    }

    public void testPacketNull() {
        NcePacketGenPanel t = new NcePacketGenPanel();
        NceMessage m = t.createPacket("");
        Assert.assertEquals("null pointer", null, m);
    }

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

    // from here down is testing infrastructure
    public NcePacketGenPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NcePacketGenPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NcePacketGenPanelTest.class);
        return suite;
    }

}
