package jmri.jmrix.loconet.locogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.locogen.LocoGenPanel class
 *
 * @author Bob Jacobsen Copyright 2001, 2003
 */
public class LocoGenTest {

    @Test
    public void testPacketNull() {
        LocoGenPanel t = new LocoGenPanel();
        LocoNetMessage m = t.createPacket("");
        assertNull( m, "null pointer");
    }

    @Test
    public void testPacketCreate() {
        LocoGenPanel t = new LocoGenPanel();
        LocoNetMessage m = t.createPacket("12 34 AB 3 19 6 B B1");
        assertEquals( 8, m.getNumDataElements(), "length");
        assertEquals( 0x12, m.getElement(0) & 0xFF, "0th byte");
        assertEquals( 0x34, m.getElement(1) & 0xFF, "1st byte");
        assertEquals( 0xAB, m.getElement(2) & 0xFF, "2nd byte");
        assertEquals( 0x03, m.getElement(3) & 0xFF, "3rd byte");
        assertEquals( 0x19, m.getElement(4) & 0xFF, "4th byte");
        assertEquals( 0x06, m.getElement(5) & 0xFF, "5th byte");
        assertEquals( 0x0B, m.getElement(6) & 0xFF, "6th byte");
        assertEquals( 0xB1, m.getElement(7) & 0xFF, "7th byte");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
