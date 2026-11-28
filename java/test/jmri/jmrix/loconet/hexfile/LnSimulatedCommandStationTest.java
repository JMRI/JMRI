package jmri.jmrix.loconet.hexfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link LnSimulatedCommandStation}: slot messages, written out byte
 * by byte from the LocoNet Personal Edition, and the replies.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LnSimulatedCommandStationTest {

    private LocoNetInterfaceScaffold lnis;
    private LnSimulatedCommandStation cs;
    private final List<String> changes = new ArrayList<>();

    private static LocoNetMessage msg(int... bytes) {
        LocoNetMessage m = new LocoNetMessage(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            m.setElement(i, bytes[i]);
        }
        return m;
    }

    private String lastReply() {
        LocoNetMessage m = lnis.outbound.lastElement();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < m.getNumDataElements(); i++) {
            b.append(i == 0 ? "" : " ").append(String.format("%02X", m.getElement(i)));
        }
        return b.toString();
    }

    @Test
    public void testAcquireDriveRelease() {
        cs.message(msg(0xBF, 0x00, 0x03, 0x00)); // OPC_LOCO_ADR for short 3
        // slot 1, common, 128 steps, address 3, stopped, forward, track power on
        assertEquals("E7 0E 01 13 03 00 00 07 00 00 00 00 00 " + String.format("%02X",
                0xFF ^ 0xE7 ^ 0x0E ^ 0x01 ^ 0x13 ^ 0x03 ^ 0x07), lastReply());
        cs.message(msg(0xBA, 0x01, 0x01, 0x00)); // null move: into use
        assertTrue(lastReply().startsWith("E7 0E 01 33 03"), lastReply());
        cs.message(msg(0xA0, 0x01, 0x40, 0x00)); // speed 64
        cs.message(msg(0xA1, 0x01, 0x20, 0x00)); // reverse
        assertEquals(64, cs.getSlot(1).getSpeed());
        assertFalse(cs.getSlot(1).isForward());
        cs.message(msg(0xBF, 0x00, 0x03, 0x00)); // asked for again: the same slot
        assertTrue(lastReply().startsWith("E7 0E 01 33 03 40 20"), lastReply());
        cs.message(msg(0xB5, 0x01, 0x13, 0x00)); // released to common
        assertEquals(0x10, cs.getSlot(1).getStatus());
        assertEquals(List.of("1:3:0", "1:3:64", "1:3:64r", "1:3:64r"), changes);
    }

    @Test
    public void testLongAddressAndSlotWrite() {
        cs.message(msg(0xBF, 0x09, 0x52, 0x00)); // 1234 = 9 * 128 + 82
        assertEquals(1234, cs.getSlot(1).getAddress());
        assertTrue(lastReply().startsWith("E7 0E 01 13 52 00 00 07 00 09"), lastReply());
        // OPC_WR_SL_DATA: slot 1 in use, 28 steps, speed 10, forward
        cs.message(msg(0xEF, 0x0E, 0x01, 0x30, 0x52, 0x0A, 0x00, 0x07, 0x00, 0x09, 0x00, 0x01, 0x02, 0x00));
        assertEquals("B4 6F 7F " + String.format("%02X", 0xFF ^ 0xB4 ^ 0x6F ^ 0x7F), lastReply(), "acknowledged");
        assertEquals(0, cs.getSlot(1).getDecoderType());
        assertEquals(10, cs.getSlot(1).getSpeed());
    }

    @Test
    public void testNoFreeSlot() {
        for (int a = 1; a <= 3; a++) {
            cs.message(msg(0xBF, 0x00, a, 0x00));
        }
        cs.message(msg(0xBF, 0x00, 0x04, 0x00));
        assertEquals("B4 3F 00 " + String.format("%02X", 0xFF ^ 0xB4 ^ 0x3F), lastReply(), "no free slot");
        cs.message(msg(0xBB, 0x02, 0x00, 0x00)); // read slot 2
        assertTrue(lastReply().startsWith("E7 0E 02 13 02"), lastReply());
        int n = lnis.outbound.size();
        cs.message(msg(0xBB, 0x7B, 0x00, 0x00)); // the fast clock slot: not answered
        assertEquals(n, lnis.outbound.size());
        assertNull(cs.getSlot(4));
    }

    @Test
    public void testDispatchPut() {
        cs.message(msg(0xBF, 0x00, 0x03, 0x00));
        cs.message(msg(0xBA, 0x01, 0x01, 0x00));
        cs.message(msg(0xBA, 0x01, 0x00, 0x00)); // move to slot 0: dispatch
        assertEquals(0x10, cs.getSlot(1).getStatus());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold(new LocoNetSystemConnectionMemo());
        cs = new LnSimulatedCommandStation(lnis, 3);
        cs.addSlotListener((s, t) -> changes.add(s.getNumber() + ":" + s.getAddress() + ":" + s.getSpeed()
                + (s.isForward() ? "" : "r")));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
