package jmri.jmrix.loconet;

import java.util.Arrays;
import java.util.List;

import jmri.NmraPacket;
import jmri.*;
import jmri.jmrix.loconet.SlotMapEntry.SlotType;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author B. Milhaupt, Copyright (C) 2018
 */
public class SlotManagerTest {

    /**
     * Local member to recall when a SlotListener has been invoked.
     */
    private LocoNetSlot testSlot;

    @Test
    public void testGetDirectFunctionAddressOK() {
        LocoNetMessage m1;

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // short 03 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x24);
        m1.setElement(4, 0x02);
        m1.setElement(5, 0x03);
        m1.setElement(6, 0x21);
        m1.setElement(7, 0x00);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x62);
        assertEquals( 3, slotmanager.getDirectFunctionAddress(m1),
            "short 3 sets F9");

        // test  top half of short 65
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x42, 0x21, 0x00, 0x00, 0x23});
        assertEquals( 66, slotmanager.getDirectFunctionAddress(m1),
            "long 65 sets F9");

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // long 513 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x34);
        m1.setElement(4, 0x05);
        m1.setElement(5, 0x42);
        m1.setElement(6, 0x01);
        m1.setElement(7, 0x21);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x35);
        assertEquals( 513, slotmanager.getDirectFunctionAddress(m1),
            "long 513 sets F9");
        //test mid high address 4097
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x50, 0x01, 0x21, 0x00, 0x00, 0x27});
        assertEquals( 4097, slotmanager.getDirectFunctionAddress(m1),
            "long 4097 sets F9");
        // test high high address 9983
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x07, 0x66, 0x7F, 0x21, 0x00, 0x00, 0x6D});
        assertEquals( 9983, slotmanager.getDirectFunctionAddress(m1),
            "long 9983 sets F9");
   }

    @Test
    public void testGetDirectDccPacketOK() {
        LocoNetMessage m1;

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // short 03 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x24);
        m1.setElement(4, 0x02);
        m1.setElement(5, 0x03);
        m1.setElement(6, 0x21);
        m1.setElement(7, 0x00);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x62);
        assertEquals( 0xA1, slotmanager.getDirectDccPacket(m1),
            "short 3 sets F9");

        // test  top half of short 65
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x42, 0x21, 0x00, 0x00, 0x23});
        assertEquals( 0xA1, slotmanager.getDirectDccPacket(m1),
            "long 65 sets F9");

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // long 513 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x34);
        m1.setElement(4, 0x05);
        m1.setElement(5, 0x42);
        m1.setElement(6, 0x01);
        m1.setElement(7, 0x21);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x35);
        assertEquals( 0xA1, slotmanager.getDirectDccPacket(m1),
            "long 513 sets F9");
        //test mid high address 4097
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x05, 0x50, 0x01, 0x21, 0x00, 0x00, 0x27});
        assertEquals( 0xA1, slotmanager.getDirectDccPacket(m1),
            "long 4097 sets F9");
        // test high high address 9983
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x07, 0x66, 0x7F, 0x21, 0x00, 0x00, 0x6D});
        assertEquals( 0xA1, slotmanager.getDirectDccPacket(m1),
            "long 9983 sets F9");
    }

    @Test
    public void testGetDirectFunctionAddressOK_F21_F28() {
        LocoNetMessage m1;

        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x06, 0x03, 0x5F, 0x00, 0x00, 0x00, 0x08});
        assertEquals( 3, slotmanager.getDirectFunctionAddress(m1),
            "short 3 sets F28");
        // test  top half of short 66
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x42, 0x21, 0x00, 0x00, 0x23});
        assertEquals( 66, slotmanager.getDirectFunctionAddress(m1),
            "long 66 sets F28");
        // address 3
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0D, 0x42, 0x01, 0x5F, 0x00, 0x00, 0x33});
        assertEquals( 513, slotmanager.getDirectFunctionAddress(m1),
            "long 513 sets F28");
        //test mid high address 4097
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0D, 0x50, 0x01, 0x5F, 0x00, 0x00, 0x21});
        assertEquals( 4097, slotmanager.getDirectFunctionAddress(m1),
            "long 4097 sets F28");
        // test high high address 9983
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0F, 0x66, 0x7F, 0x5F, 0x00, 0x00, 0x6B});
        assertEquals( 9983, slotmanager.getDirectFunctionAddress(m1),
            "long 9983 sets F9");
   }

    @Test
    public void testisExtFunctionMessage_F21_F28() {
        LocoNetMessage m1;

        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x34, 0x06, 0x03, 0x5F, 0x00, 0x00, 0x00, 0x08});
        assertTrue( slotmanager.isExtFunctionMessage(m1), "short 3 sets F28");
        // test  top half of short 66
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x24, 0x02, 0x42, 0x21, 0x00, 0x00, 0x23});
        assertTrue( slotmanager.isExtFunctionMessage(m1), "short 66 sets F28");
        // address 3
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0D, 0x42, 0x01, 0x5F, 0x00, 0x00, 0x33});
        assertTrue( slotmanager.isExtFunctionMessage(m1), "short 513 sets F28");
        //test mid high address 4097
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0D, 0x50, 0x01, 0x5F, 0x00, 0x00, 0x21});
        assertTrue( slotmanager.isExtFunctionMessage(m1), "short 4097 sets F28");
        // test high high address 9983
        m1 = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7F, 0x44, 0x0F, 0x66, 0x7F, 0x5F, 0x7F, 0x00, 0x14});
        assertTrue( slotmanager.isExtFunctionMessage(m1), "short 9983 sets F28");
    }

    @Test
    public void testGetSlotSend() {
        testSlot = null;
        SlotListener p2 = (LocoNetSlot l) -> {
            testSlot = l;
        };
        slotmanager.slotFromLocoAddress(0x2134, p2);
        assertEquals( "BF 42 34 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "slot request message");
        assertEquals( 1, slotmanager.mLocoAddrHash.size(), "hash length");
        assertEquals( true, slotmanager.mLocoAddrHash.containsKey(0x2134),
            "key present");
        assertEquals( true, slotmanager.mLocoAddrHash.contains(p2),
            "value present");
    }

    @Test
    public void testGetSlotRcv() {
        testSlot = null;
        SlotListener p2 = (LocoNetSlot l) -> {
            testSlot = l;
        };
        slotmanager.slotFromLocoAddress(0x2134, p2);
        // echo of the original message
        LocoNetMessage m1 = new LocoNetMessage(4);
        m1.setOpCode(0xBF);
        m1.setElement(1, 0x42);
        m1.setElement(2, 0x34);
        slotmanager.message(m1);
        // reply says its in slot 4
        LocoNetMessage m2 = new LocoNetMessage(14);
        m2.setElement(0, 0xE7);
        m2.setElement(1, 0xE);
        m2.setElement(2, 0xB);
        m2.setElement(3, 3);
        m2.setElement(4, 0x34);
        m2.setElement(5, 0);
        m2.setElement(6, 0);
        m2.setElement(7, 4);
        m2.setElement(8, 0);
        m2.setElement(9, 0x42);
        m2.setElement(10, 0);
        m2.setElement(11, 0);
        m2.setElement(12, 0);
        m2.setElement(13, 0x6c);
        slotmanager.message(m2);
        assertEquals( slotmanager.slot(11), testSlot, "returned slot");
        // and make sure it forgets
        testSlot = null;
        slotmanager.message(m1);
        slotmanager.message(m2);
        assertNull( testSlot, "returned slot");
    }

    @Test
    public void testReadCVPaged() throws ProgrammerException {
        String CV1 = "12";
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.readCV(CV1, lstn);
        assertEquals( "EF 0E 7C 23 00 00 00 00 00 0B 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testReadCVRegister() throws ProgrammerException {
        String CV1 = "2";
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.readCV(CV1, lstn);
        assertEquals( "EF 0E 7C 13 00 00 00 00 00 01 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testReadCVDirect() throws ProgrammerException {
        log.debug(".... start testReadCVDirect ...");
        String CV1 = "29";
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.readCV(CV1, lstn);
        assertEquals( "EF 0E 7C 2B 00 00 00 00 00 1C 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "read message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence)
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;

        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedLongTimer;},"startedLongTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedLongTimer, "started long timer");
        assertFalse( startedShortTimer, "didn't start short timer");

        // read received back (DCS240 sequence)
        value = 0;
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B}));
        JUnitUtil.waitFor(()->{return value == 35;},"value == 35 not set");
        log.debug("checking..");
        assertEquals( 0, status, "reply status");
        assertEquals( 35, value, "reply value");
        assertTrue( stoppedTimer, "timer stopped");

        log.debug(".... end testReadCVDirect ...");
    }

    @Test
    public void testReadCVDirectWithDT6() throws ProgrammerException {
        // test with extra 0x7F reply
        log.debug(".... start testReadCVDirectWDT6 ...");
        String CV1 = "29";
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.readCV(CV1, lstn);
        assertEquals( "EF 0E 7C 2B 00 00 00 00 00 1C 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACKs received back (DCS240 + DT6 sequence)
        startedShortTimer = false;
        startedLongTimer = false;

        log.debug("send 1st LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x7F, 0x5B}));
        assertFalse( startedLongTimer, "long timer not started yet");

        log.debug("send 2nd LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedLongTimer;},"startedLongTimer not set");

        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedLongTimer, "started long timer");
        assertFalse( startedShortTimer, "didn't start short timer");

        // read received back (DCS240 sequence)
        value = 0;
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B}));
        JUnitUtil.waitFor(()->{return value == 35;},"value == 35 not set");
        log.debug("checking..");
        assertEquals( 0, status, "reply status");
        assertEquals( 35, value, "reply value");

        log.debug(".... end testReadCVDirectWithDT6 ...");
    }

    @Test
    public void testReadCVFromBoardOnDCS240andDT602() throws ProgrammerException {
        // test with extra 0x7F reply
        log.debug(".... start testReadCVFromBoardOnDCS240andDT602 ...");

        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        LnProgrammerManager t = new LnProgrammerManager(memo);
        var programmer = t.getAddressedProgrammer(true, 11010);
        assertNotNull(programmer);

        String CV1 = "2";
        programmer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        programmer.readCV(CV1, lstn);
        assertEquals( "EF 0E 7C 2F 00 56 02 00 00 01 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACKs received back (DCS240 + DT6 sequence)
        log.debug("send 1st LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x7F, 0x5B}));

        log.debug("send 2nd LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));

        // read received back (DCS240 sequence)
        value = 0;
        log.debug("send E7 reply back with value 35");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B}));
        JUnitUtil.waitFor(()->{return value == 35;},"value == 35 not set");
        log.debug("checking..");
        assertEquals( 0, status, "reply status");
        assertEquals( 35, value, "reply value");

        log.debug(".... end testReadCVFromBoardOnDCS240andDT602 ...");
    }

    @Test
    public void testReadCVFromBoardOnDCS100() throws ProgrammerException {
        // test with only 0x7F reply
        log.debug(".... start testReadCVFromBoardOnDCS100 ...");

        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        LnProgrammerManager t = new LnProgrammerManager(memo);
        var programmer = t.getAddressedProgrammer(true, 11010);
        assertNotNull(programmer);

        String CV1 = "2";
        // programmer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        programmer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        programmer.readCV(CV1, lstn);

        assertEquals( "EF 0E 7C 2F 00 56 02 00 00 01 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        log.debug("send 1st LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x7F, 0x5B}));

        // read received back (DCS100 sequence)
        value = 0;
        log.debug("send E7 reply back with value 35");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B}));
        JUnitUtil.waitFor(()->{return value == 35;},"value == 35 not set");
        log.debug("checking..");
        assertEquals( 0, status, "reply status");
        assertEquals( 35, value, "reply value");

        log.debug(".... end testReadCVFromBoardOnDCS100 ...");
    }

    @Test
    public void testReadCVOpsModeLong() throws ProgrammerException {
        String CV1 = "12";
        slotmanager.readCVOpsMode(CV1, lstn, 4 * 128 + 0x23, true);
        assertEquals( "EF 0E 7C 2F 00 04 23 00 00 0B 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testReadCVOpsModeShort() throws ProgrammerException {
        String CV1 = "12";
        slotmanager.readCVOpsMode(CV1, lstn, 22, false);
        assertEquals( "EF 0E 7C 2F 00 00 16 00 00 0B 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testWriteCVPaged() throws ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        assertEquals( "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVRegister() throws ProgrammerException {
        String CV1 = "2";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        assertEquals( "EF 0E 7C 53 00 00 00 00 00 01 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVDirect() throws ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVDirectStringDCS240() throws ProgrammerException {
        log.debug(".... start testWriteCVDirectStringDCS240 ...");
        String CV1 = "31";
        int val2 = 16;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence)
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");

        // read received back (DCS240 sequence)
        value = -15;
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(()->{return value == -1;},"value == -1 not set");
        log.debug("checking..");
        assertEquals( 0, status, "reply status");
        assertEquals( -1, value, "reply value");

        log.debug(".... end testWriteCVDirectStringDCS240 ...");
    }

    @Test
    public void testWriteCVDirectStringDCS240WithDT6() throws ProgrammerException {
        log.debug(".... start testWriteCVDirectStringDCS240WithDT6 ...");
        String CV1 = "31";
        int val2 = 16;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DT6 plus DCS240 sequence)
        startedShortTimer = false;
        startedLongTimer = false;

        log.debug("send 1st LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x7F, 0x5B}));
        assertFalse( startedShortTimer, "short timer not started yet");

        log.debug("send 2nd LACK back");
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");

        // read received back (DCS240 sequence)
        value = -15;
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(()->{return value == -1;},"value == -1 not set");
        log.debug("checking..");
        assertEquals( 0, status, "reply status");
        assertEquals( -1, value, "reply value");

        log.debug(".... end testWriteCVDirectStringDCS240WithDT6 ...");
    }

    @Test
    public void testLackLogic() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25});
        assertTrue( slotmanager.checkLackTaskAccepted(m.getElement(2)), "checkLackTaskAccepted(m.getElement(2))");
        assertFalse( slotmanager.checkLackProgrammerBusy(m.getElement(2)), "checkLackProgrammerBusy(m.getElement(2))");
        assertFalse( slotmanager.checkLackAcceptedBlind(m.getElement(2)), "checkLackAcceptedBlind(m.getElement(2))");
    }

    @Test
    public void testWriteCVDirectStringDCS240Interrupted() throws ProgrammerException {
        log.debug(".... start testWriteCVDirectStringDCS240 ...");
        String CV1 = "31";
        int val2 = 16;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence)
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;

        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");

        // CS check received back (DCS240 sequence)
        log.debug("send CS check back");
        slotmanager.message(new LocoNetMessage(new int[]{0xBB, 0x7F, 0x00, 0x3B}));
        // not clear what to wait for here; status doesn't change
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "post-CS-check status");

        // read received back (DCS240 sequence)
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        // not clear what to wait for here; content doesn't change
        JUnitUtil.waitFor(releaseTestDelay);
        log.debug("checking..");
        JUnitUtil.waitFor(() -> status == 0, "reply status");
        assertEquals( 0, status, "reply status");
        assertEquals( -1, value, "reply value");

        log.debug(".... end testWriteCVDirectStringDCS240 ...");
    }

    @Test
    public void testWriteCVOpsLongAddrOneLack() throws ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.setAcceptAnyLACK();
        slotmanager.writeCVOpsMode(CV1, val2, lstn, 4 * 128 + 0x23, true);
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 67 00 04 23 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
        assertEquals( -999, status, "reply status");
        assertEquals( -999, value, "reply value");

        // provide LONG_ACK: Function not implemented, no reply will follow.
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x7F}));
        JUnitUtil.waitFor(releaseTestDelay);
        JUnitUtil.waitFor(() -> status == 0, "reply status");
        assertEquals( 0, status, "reply status");
        assertEquals( -1, value, "reply value");

    }

    @Test
    public void testWriteCVOpsLongAddrTwoLacks() throws ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.writeCVOpsMode(CV1, val2, lstn, 4 * 128 + 0x23, true);
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 67 00 04 23 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
        assertEquals( -999, status, "reply status");
        assertEquals( -999, value, "reply value");

        // provide LONG_ACK: Function not implemented, no reply will follow.
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x7F}));
        // LONG_ACK: The Slot Write command was accepted blind (no response will be sent).
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x40}));
        JUnitUtil.waitFor(releaseTestDelay);
        JUnitUtil.waitFor(() -> status == 0, "reply status");
        assertEquals( 0, status, "reply status");
        assertEquals( -1, value, "reply value");

    }

    @Test
    public void testWriteCVOpsShortAddr() throws ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.writeCVOpsMode(CV1, val2, lstn, 22, false);
        assertEquals( "EF 0E 7C 67 00 00 16 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteThroughFacade() throws ProgrammerException {
        log.debug(".... start testWriteThroughFacade ...");
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);

        // install Facades from ESU_LokSoundV4_0.xml

        // <name>High Access via Double Index</name>
        String top = "256";
        String addrCVhigh = "96";
        String addrCVlow = "97";
        String valueCV = "99";
        String modulo = "100";
        jmri.implementation.AddressedHighCvProgrammerFacade pf1
                = new jmri.implementation.AddressedHighCvProgrammerFacade(slotmanager, top, addrCVhigh, addrCVlow, valueCV, modulo);

        // <name>Indexed CV access</name>
        String PI = "31";
        String SI = "16";
        boolean cvFirst = false;
        jmri.implementation.MultiIndexProgrammerFacade pf2
                = new jmri.implementation.MultiIndexProgrammerFacade(pf1, PI, SI, cvFirst, false);

        String CV1 = "16.2.257";
        int val2 = 55;

        // Start overall sequence
        pf2.writeCV(CV1, val2, lstn);

        // Check for PI write
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write PI message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to PI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( -999, status, "initial status");

        // completion received back (DCS240 sequence) to PI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "initial status");

        // check that SI write happened
        assertEquals( 2, lnis.outbound.size(), "two messages sent");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 0F 02 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "write SI message");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to SI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply
        assertEquals( 2, lnis.outbound.size(), "still two messages sent");

        // completion received back (DCS240 sequence) to SI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x0F, 0x02, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "initial status");

        // check that final CV write happened
        JUnitUtil.waitFor(() -> lnis.outbound.size() == 3, "final CV write has happened");
        assertEquals( 3, lnis.outbound.size(), "three messages sent");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 10 00 37 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write final CV message");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to final CV write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply
        assertEquals( 3, lnis.outbound.size(), "three messages sent");

        // completion received back (DCS240 sequence)
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x10, 0x00, 0x37, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(releaseTestDelay);
        log.debug("checking..");
        JUnitUtil.waitFor(() -> status == 0, "reply status");
        assertEquals( 0, status, "reply status");
        assertEquals( -1, value, "reply value");
        assertEquals( 3, lnis.outbound.size(), "three messages sent");

        log.debug(".... end testWriteThroughFacade ...");
    }

    @Test
    public void testReadThroughFacade() throws ProgrammerException {
        log.debug(".... start testReadThroughFacade ...");
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);

        // install Facades from ESU_LokSoundV4_0.xml

        // <name>High Access via Double Index</name>
        String top = "256";
        String addrCVhigh = "96";
        String addrCVlow = "97";
        String valueCV = "99";
        String modulo = "100";
        jmri.implementation.AddressedHighCvProgrammerFacade pf1
                = new jmri.implementation.AddressedHighCvProgrammerFacade(slotmanager, top, addrCVhigh, addrCVlow, valueCV, modulo);

        // <name>Indexed CV access</name>
        String PI = "31";
        String SI = "16";
        boolean cvFirst = false;
        jmri.implementation.MultiIndexProgrammerFacade pf2
                = new jmri.implementation.MultiIndexProgrammerFacade(pf1, PI, SI, cvFirst, false);

        String CV1 = "16.2.257";
        // Start overall sequence
        pf2.readCV(CV1, lstn);

        // Check for PI write
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write PI message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to PI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( -999, status, "initial status");

        // completion received back (DCS240 sequence) to PI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "initial status");

        // check that SI write happened
        JUnitUtil.waitFor(() -> {
            return 2 == lnis.outbound.size();
        }, "two messages sent");

        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 0F 02 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write SI message");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to SI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedShortTimer, "started short timer");
        assertFalse( startedLongTimer, "didn't start long timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply

        JUnitUtil.waitFor(() -> {
            return 2 == lnis.outbound.size();
        }, "still two messages sent");

        // completion received back (DCS240 sequence) to SI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x0F, 0x02, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "initial status");

        // check that final CV write happened
        JUnitUtil.waitFor(() -> {
            return 3 == lnis.outbound.size();
        }, "three messages sent ");
        assertEquals( "EF 0E 7C 2B 00 00 00 00 10 00 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write final CV message");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to final CV write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(releaseTestDelay);
        assertEquals( -999, status, "post-LACK status");
        assertTrue( startedLongTimer, "started long timer");
        assertFalse( startedShortTimer, "didn't start short timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply
        assertEquals( 3, lnis.outbound.size(), "three messages sent");

        // completion received back (DCS240 sequence)
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x10, 0x00, 0x37, 0x7F, 0x7F, 0x4A}));
        assertEquals( -999, status, "no immediate reply");
        JUnitUtil.waitFor(releaseTestDelay);
        log.debug("checking..");
        JUnitUtil.waitFor(() -> status == 0, "reply status");
        assertEquals( 0, status, "reply status");
        assertEquals( 55, value, "reply value");

        JUnitUtil.waitFor(() -> {
            return 3 == lnis.outbound.size();
        }, "three messages sent");

        log.debug(".... end testReadThroughFacade ...");
    }

    @Test
    public void testReadThroughFacadeFail() throws ProgrammerException {
        log.debug(".... start testReadThroughFacadeFail ...");
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);

        // install Facades from ESU_LokSoundV4_0.xml

        // <name>High Access via Double Index</name>
        String top = "256";
        String addrCVhigh = "96";
        String addrCVlow = "97";
        String valueCV = "99";
        String modulo = "100";
        jmri.implementation.AddressedHighCvProgrammerFacade pf1
                = new jmri.implementation.AddressedHighCvProgrammerFacade(slotmanager, top, addrCVhigh, addrCVlow, valueCV, modulo);

        // <name>Indexed CV access</name>
        String PI = "31";
        String SI = "16";
        boolean cvFirst = false;
        jmri.implementation.MultiIndexProgrammerFacade pf2
                = new jmri.implementation.MultiIndexProgrammerFacade(pf1, PI, SI, cvFirst, false);

        String CV1 = "16.2.257";
        // Start overall sequence
        pf2.readCV(CV1, lstn);

        // Check for PI write
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write PI message");
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( -999, status, "initial status");

        // LACK received back (DCS240 sequence) to PI write: rejected
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x0, 0x24}));
        JUnitUtil.waitFor(releaseTestDelay);
        JUnitUtil.waitFor(() -> status == 4, "reply status");
        assertEquals( 4, status, "post-LACK status is fail");
        assertFalse( startedShortTimer, "didn't start short timer");
        assertFalse( startedLongTimer, "didn't start long timer");
        JUnitUtil.waitFor(releaseTestDelay);  // wait for slow reply
        assertEquals( 1, lnis.outbound.size(), "still one message sent");

        log.debug(".... end testReadThroughFacadeFail ...");
    }

    @Test
    public void testGetProgrammingModes() {
        List<ProgrammingMode> l = slotmanager.getSupportedModes();
        assertEquals( 5, l.size(), "programming mode list length ok");
        assertEquals( ProgrammingMode.DIRECTBYTEMODE, l.get(0), "programming mode 0");
        assertEquals( ProgrammingMode.PAGEMODE, l.get(1), "programming mode 1");
        assertEquals( ProgrammingMode.REGISTERMODE, l.get(2), "programming mode 2");
        assertEquals( ProgrammingMode.ADDRESSMODE, l.get(3), "programming mode 3");
        assertEquals( "LOCONETCSOPSWMODE", l.get(4).getStandardName(), "programming mode 4");
    }

    @Test
    public void testSendPacket() {
        byte msg[] = NmraPacket.accDecPktOpsMode(1, 4, 53);
        slotmanager.sendPacket(msg, 1);
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 35 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 1");

        msg = NmraPacket.accDecPktOpsMode(128, 4, 53);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 51 07 00 50 6C 03 35 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 2");

        msg= NmraPacket.accDecPktOpsMode(256, 4, 53);
        slotmanager.sendPacket(msg, 3);
        assertEquals( "ED 0B 7F 52 07 00 30 6C 03 35 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 3");

        msg = NmraPacket.accDecPktOpsMode(1, 37, 53);
        slotmanager.sendPacket(msg, 4);
        assertEquals( "ED 0B 7F 53 07 01 70 6C 24 35 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 4");

        msg = NmraPacket.accDecPktOpsMode(1, 129, 53);
        slotmanager.sendPacket(msg, 5);
        assertEquals( "ED 0B 7F 54 0F 01 70 6C 00 35 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 5");

        msg = NmraPacket.accDecPktOpsMode(1, 10, 0);
        slotmanager.sendPacket(msg, 6);
        assertEquals( "ED 0B 7F 55 07 01 70 6C 09 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 6");

        msg = NmraPacket.accDecPktOpsMode(1, 10, 128);
        slotmanager.sendPacket(msg, 7);
        assertEquals( "ED 0B 7F 56 17 01 70 6C 09 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 7");

        msg = NmraPacket.accDecPktOpsMode(1, 10, 255);
        slotmanager.sendPacket(msg, 8);
        assertEquals( "ED 0B 7F 57 17 01 70 6C 09 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 8");

        msg = NmraPacket.accDecPktOpsMode(511, 255, 0);
        slotmanager.sendPacket(msg, 9);
        JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' reduced from 9 to 8.");
        assertEquals( "ED 0B 7F 57 0F 3F 00 6C 7E 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 9");

        msg = NmraPacket.accSignalDecoderPkt(1, 31);
        slotmanager.sendPacket(msg, 0);
        JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' of 0 is illegal and is forced to 1.");
        assertEquals( "ED 0B 7F 30 01 01 71 1F 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(2, 30);
        slotmanager.sendPacket(msg, -1);
        JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' of -1 is illegal and is forced to 1.");
        assertEquals( "ED 0B 7F 30 01 01 73 1E 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(4, 29);
        slotmanager.sendPacket(msg, 3);
        assertEquals( "ED 0B 7F 32 01 01 77 1D 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(8, 27);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 02 77 1B 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(16, 23);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 04 77 17 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(32, 15);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 08 77 0F 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(64, 1);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 10 77 01 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(128, 0);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 20 77 00 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(256, 2);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 00 67 02 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(512, 4);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 00 57 04 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(1024, 8);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 00 37 08 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");

        msg = NmraPacket.accSignalDecoderPkt(511, 16);
        slotmanager.sendPacket(msg, 2);
        assertEquals( "ED 0B 7F 31 01 00 55 10 00 00 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "nmra packet 10");
    }

    @Test
    public void testOpcImmPacketRetry() {
        byte msg[] = NmraPacket.accDecPktOpsMode(1, 4, 54);
        slotmanager.sendPacket(msg, 1);
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 36 00",
            lnis.outbound.elementAt(0).toString(), "nmra packet 1 retry test");
        slotmanager.message(lnis.outbound.get(0));
        assertNotNull( slotmanager.immedPacket, "check immedPacket non null");

        assertEquals( new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x36, 0x00}),
            slotmanager.immedPacket,
            "check that slotmanager remembers last opc_imm_packet (1)");
        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6D, 0x00, 0x00}));

        JUnitUtil.waitFor(()->{return lnis.outbound.size() >1;},"retry message");
        assertEquals( 2, lnis.outbound.size(), "retry test two messages sent");
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 36 00",
            lnis.outbound.elementAt(1).toString(),
            "nmra packet 2 retry test");

        msg = NmraPacket.accDecPktOpsMode(1, 4, 55);
        slotmanager.sendPacket(msg, 1);
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 37 00",
            lnis.outbound.elementAt(2).toString(),
            "nmra packet 2 retry test");
        assertEquals( 3, lnis.outbound.size(), "retry test two messages sent");
        slotmanager.message(lnis.outbound.get(2));
        assertEquals( new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x37, 0x00}),
            slotmanager.immedPacket,
            "check that slotmanager remembers last opc_imm_packet (2)");

        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6D, 0x01, 0x00}));

        assertEquals( 3, lnis.outbound.size(), "retry test two (b) no new message sent");

        msg = NmraPacket.accDecPktOpsMode(1, 4, 56);
        slotmanager.sendPacket(msg, 1);
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 38 00",
            lnis.outbound.elementAt(3).toString(), "nmra packet 3 retry test");
        assertEquals( 4, lnis.outbound.size(), "retry test three (a) messages sent");
        slotmanager.message(lnis.outbound.get(3));
        assertEquals( new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x38, 0x00}),
            slotmanager.immedPacket, "check that slotmanager remembers last opc_imm_packet (3)");

        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6C, 0x00, 0x00}));

        assertEquals( 4, lnis.outbound.size(), "retry test three no new message sent");


        msg = NmraPacket.accDecPktOpsMode(1, 4, 57);
        slotmanager.sendPacket(msg, 1);
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 39 00",
            lnis.outbound.elementAt(4).toString(), "nmra packet 4 retry test");
        assertEquals( 5, lnis.outbound.size(), "retry test four (a) messages sent");
        slotmanager.message(lnis.outbound.get(4));
        assertEquals( new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x39, 0x00}),
            slotmanager.immedPacket,
            "check that slotmanager remembers last opc_imm_packet (4)");
        JUnitUtil.waitFor(500); // wait 1/2 second to ensure a retry does not happen.
        assertEquals( 5, lnis.outbound.size(), "retry test four (b) messages sent");

        slotmanager.message(new LocoNetMessage(new int[] {0xB3, 0x6d, 0x00, 0x00}));

        assertFalse( slotmanager.mTurnoutNoRetry, "check mTurnoutNoRetry");
        slotmanager.setThrottledTransmitter(null, true);
        assertTrue( slotmanager.mTurnoutNoRetry, "check mTurnoutNoRetry (2)");

        msg = NmraPacket.accDecPktOpsMode(1, 4, 33);
        slotmanager.sendPacket(msg, 1);
        assertEquals( "ED 0B 7F 50 07 01 70 6C 03 21 00",
            lnis.outbound.elementAt(5).toString(),
            "nmra packet 5 retry test");
        assertEquals( 6, lnis.outbound.size(), "retry test five (a) messages sent");
        slotmanager.message(lnis.outbound.get(5));
        assertEquals( new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x21, 0x00}),
            slotmanager.immedPacket,
            "check that slotmanager remembers last opc_imm_packet (5)");

        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6C, 0x00, 0x00}));

        JUnitUtil.waitFor(500); // wait 1/2 second to ensure a retry does not happen.
        assertEquals( 6, lnis.outbound.size(), "retry test five (b) messages sent");

    }

    @Test
    public void testSlotMessageOpcPktImmFunctions() {
        testSlot = null;
        SlotListener p2 = (LocoNetSlot l) -> {
            testSlot = l;
        };
        slotmanager.slotFromLocoAddress(4121, p2);
        assertEquals( 1, lnis.outbound.size(), "number of transmitted messages so far (a)");
        assertEquals( "BF 20 19 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "slot request message");
        assertEquals( 1, slotmanager.mLocoAddrHash.size(), "hash length");
        assertTrue( slotmanager.mLocoAddrHash.containsKey(4121),
            "key present");
        assertTrue( slotmanager.mLocoAddrHash.contains(p2),
            "value present");
        assertNull( testSlot, "check testSlot still null (a)");

        // reflect the transmitted message back to the slot manager
        slotmanager.message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // reply says its in slot 10
        LocoNetMessage m2 = new LocoNetMessage(14);
        m2.setElement(0, 0xE7);
        m2.setElement(1, 0x0E);
        m2.setElement(2, 0x0A);  // slot 10
        m2.setElement(3, 3);
        m2.setElement(4, 0x19);
        m2.setElement(5, 0);
        m2.setElement(6, 0);
        m2.setElement(7, 4);
        m2.setElement(8, 0);
        m2.setElement(9, 0x20);
        m2.setElement(10, 0);
        m2.setElement(11, 0);
        m2.setElement(12, 0);
        m2.setElement(13, 0x6c);
        slotmanager.message(m2);

        assertNotNull( testSlot, "check testSlot not null (a)");
        assertEquals( LnConstants.LOCO_FREE, testSlot.slotStatus(), "check slot status");

        assertEquals( slotmanager.slot(10), testSlot, "returned slot");

        assertEquals( 4121, testSlot.locoAddr(), "check slot has correct address");
        assertFalse( testSlot.localF9, "check default F9 state for slot");
        assertFalse( testSlot.isF10(), "check default F10 state for slot");
        assertFalse( testSlot.isF11(), "check default F11 state for slot");
        assertFalse( testSlot.isF12(), "check default F12 state for slot");

        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        LocoNetMessage m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.message(m);

        assertFalse( testSlot.isF9(), "check slot f9 - message wasn't accepted account slot 'free'");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");
        assertEquals( 4121, slotmanager.getDirectFunctionAddress(m),
            "check message with correct format versus getDirectFunctionAddress");

        // [ED 0A 7F 34 05 50 19 21 00  3E]  is not a send packet immediate message!
        m = new LocoNetMessage(new int[] {0xED, 0x0a, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x3e});
        slotmanager.message(m);

        assertFalse( testSlot.isF9(), "check slot f9 - message wasn't accepted account wrong message length");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");
        assertEquals( -1, slotmanager.getDirectFunctionAddress(m),
            "check message with wrong message length versus getDirectFunctionAddress");


        // [ED 0B 7e 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7e, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.message(m);

        assertFalse( testSlot.isF9(), "check slot f9 - message wasn't accepted account wrong byte 2 value");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");
        assertEquals( -1, slotmanager.getDirectFunctionAddress(m),
            "check message with wrong byte 2 value versus getDirectFunctionAddress");

        m.setElement(2, 0x7f);
        m.setElement(3, 0x10);
        assertEquals( -1, slotmanager.getDirectFunctionAddress(m),
            "check message with wrong byte 3 value(a) versus getDirectFunctionAddress");
        m.setElement(3, 0x10);
        assertEquals( -1, slotmanager.getDirectFunctionAddress(m),
            "check message with wrong byte 3 value(b) versus getDirectFunctionAddress");

        LocoNetMessage m3 = new LocoNetMessage(new int[] {0xba, 0x0a, 0x0a, 0x00});
        slotmanager.message(m3);

        m2.setElement(3,0x33);
        slotmanager.message(m2);
        assertEquals( LnConstants.LOCO_IN_USE, slotmanager.slot(10).slotStatus(),
            "check slot status in-use");
        assertEquals( -1, slotmanager.getDirectFunctionAddress(m2),
            "check slot move message versus getDirectFunctionAddress");


        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.message(m);

        assertTrue( testSlot.isF9(), "check slot f9 - message was accepted");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");
    }

    @Test
    public void testForwardMessageToSlotExceptions() {
        assertEquals( 0, slotmanager.getInUseCount(), "check 'slot in use' count is zero");
        testSlot = null;
        SlotListener p2 = (LocoNetSlot l) -> {
            testSlot = l;
        };
        slotmanager.slotFromLocoAddress(4120, p2);
        assertEquals( 1, lnis.outbound.size(), "number of transmitted messages so far (a)");
        assertEquals( "BF 20 18 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(), "slot request message");
        assertNull( testSlot, "check testSlot still null (a)");

        // reflect the transmitted message back to the slot manager
        slotmanager.message(lnis.outbound.elementAt(lnis.outbound.size()-1));

        // reply says its in slot 9
        LocoNetMessage m2 = new LocoNetMessage(14);
        m2.setElement(0, 0xE7);
        m2.setElement(1, 0x0E);
        m2.setElement(2, 0x09);  // slot 9
        m2.setElement(3, 3);
        m2.setElement(4, 0x18);
        m2.setElement(5, 0);
        m2.setElement(6, 0);
        m2.setElement(7, 4);
        m2.setElement(8, 0);
        m2.setElement(9, 0x20);
        m2.setElement(10, 0);
        m2.setElement(11, 0);
        m2.setElement(12, 0);
        m2.setElement(13, 0x6c);
        slotmanager.message(m2);

        assertNotNull( testSlot, "check testSlot not null (a)");
        assertEquals( LnConstants.LOCO_FREE, testSlot.slotStatus(), "check slot status");

        assertEquals( slotmanager.slot(9), testSlot, "returned slot");

        assertEquals( 4120, testSlot.locoAddr(), "check slot has correct address");
        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4120 set F9=On, F10=Off, F11=Off, F12=Off.
        LocoNetMessage m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.forwardMessageToSlot(m, 9);
        JUnitAppender.assertErrorMessage("slot rejected LocoNetMessage ED 0B 7F 34 05 50 19 21 00 00 3F");

        assertFalse( testSlot.isF9(), "check slot f9 - message wasn't accepted account slot 'free'");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");

        LocoNetMessage m3 = new LocoNetMessage(new int[] {0xba, 0x09, 0x09, 0x00});
        slotmanager.message(m3);

        m2.setElement(3,0x33);
        slotmanager.message(m2);
        assertEquals( LnConstants.LOCO_IN_USE, slotmanager.slot(9).slotStatus(),
            "check slot status in-use");

        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.forwardMessageToSlot(m, 9);
        JUnitAppender.assertErrorMessage("slot rejected LocoNetMessage ED 0B 7F 34 05 50 19 21 00 00 3F");

        assertFalse( testSlot.isF9(), "check slot f9 - message was not accepted (wrong address)");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");

        slotmanager.forwardMessageToSlot(m, -1);
        JUnitAppender.assertErrorMessage("Received slot number -1 is greater than array length 433 Message was ED 0B 7F 34 05 50 19 21 00 00 3F");

        assertFalse( testSlot.isF9(), "check slot f9 - message was not accepted (slot number too low)");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");

        slotmanager.forwardMessageToSlot(m, 434);
        JUnitAppender.assertErrorMessage("Received slot number 434 is greater than array length 433 Message was ED 0B 7F 34 05 50 19 21 00 00 3F");

        assertFalse( testSlot.isF9(), "check slot f9 - message was not accepted (slot number too high)");
        assertFalse( testSlot.isF10(), "check slot f10");
        assertFalse( testSlot.isF11(), "check slot f11");
        assertFalse( testSlot.isF12(), "check slot f12");

        assertEquals( 1, slotmanager.getInUseCount(), "check 'slot in use' count is one");
    }

    @Test
    public void testGetWriteConfirmMode() {
        assertEquals( jmri.Programmer.WriteConfirmMode.DecoderReply,
            slotmanager.getWriteConfirmMode("abcd"),
            "check geWriteConfirmMode('abcd')");
    }

    @Test
    public void testGetUserName() {
        assertEquals("LocoNet", slotmanager.getUserName(), "check getUserName");
    }

    @Test
    public void testOpCode8a() {

        LocoNetMessage m = new LocoNetMessage(new int[] {0x8a, 0x75});

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS100;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        assertEquals( 0, lnis.outbound.size(), "check no messages sent when DCS100");


        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS051;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        assertEquals( 0, lnis.outbound.size(), "check no messages sent when DCS051");

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS050;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        assertEquals( 0, lnis.outbound.size(), "check no messages sent when DCS050");

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DB150;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        assertEquals( 0, lnis.outbound.size(), "check no messages sent when DB150");

    }

    @Test
    public void testMoreOpCode8a() {

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS210;
        slotmanager.message(new LocoNetMessage(new int[] {0x8a, 0x75}));
        JUnitUtil.waitFor(()->{return lnis.outbound.size() >126;},"testOpCode8a: slot managersent at least 127 LocoNet messages");
        for (int i = 0; i < 127; ++i) {
            assertEquals( 0xBB, lnis.outbound.get(i).getOpCode(), "testOpCode8a DCS210: loop "+i+" check sent opcode");
            assertEquals( i, lnis.outbound.get(i).getElement(1), "testOpCode8a DCS210: loop "+i+" check sent byte 1");
            assertEquals( 0, lnis.outbound.get(i).getElement(2), "testOpCode8a DCS210: loop "+i+" check sent byte 2");

        }
    }

    @Test
    public void testEvenMoreOpCode8a() {

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS052;
        slotmanager.message(new LocoNetMessage(new int[] {0x8a, 0x75}));
        JUnitUtil.waitFor(()->{return lnis.outbound.size() >126;},"testOpCode8a: slot managersent at least 127 LocoNet messages");
        for (int i = 0; i < 127; ++i) {
            assertEquals( 0xBB, lnis.outbound.get(i).getOpCode(), "testOpCode8a DCS052: loop "+i+" check sent opcode");
            assertEquals( i, lnis.outbound.get(i).getElement(1), "testOpCode8a DCS052: loop "+i+" check sent byte 1");
            assertEquals( 0, lnis.outbound.get(i).getElement(2), "testOpCode8a DCS052: loop "+i+" check sent byte 2");

        }
    }

    @Test
    public void testYetMoreOpCode8a() {

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS240;
        slotmanager.message(new LocoNetMessage(new int[] {0x8a, 0x75}));
        JUnitUtil.waitFor(()->{return lnis.outbound.size() >126;},"testOpCode8a: slot managersent at least 127 LocoNet messages");
        for (int i = 0; i < 127; ++i) {
            assertEquals( 0xBB, lnis.outbound.get(i).getOpCode(), "testOpCode8a: loop "+i+" check sent opcode");
            assertEquals( i, lnis.outbound.get(i).getElement(1), "testOpCode8a: loop "+i+" check sent byte 1");
            assertEquals( 0, lnis.outbound.get(i).getElement(2), "testOpCode8a: loop "+i+" check sent byte 2");

        }
    }

    @Test
    public void testSetCommon() {
        // Set slot 5 common, request slot 5
        slotmanager.message(new LocoNetMessage(new int[]{0xB5, 0x05, 0x17, 0x00}));
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        }, "Requests slot read 5 after delay");
        assertEquals( "BB 05 00 00",
            lnis.outbound.elementAt(0).toString(),
            "Request read slot 5");
    }

    @Test
    public void testMove_NullMove() {
        // slot move 4 > 4 read nothing after delay.
        slotmanager.message(new LocoNetMessage(new int[]{0xba, 0x01, 0x01, 0x00}));
        JUnitUtil.waitFor(200);
        assertEquals( 0, lnis.outbound.size(), "No Outbound Data");
    }

   @Test
    public void testMove_TrueMove() {
        // slot move 1 > 2 read 1 after delay.
        slotmanager.message(new LocoNetMessage(new int[]{0xba, 0x01, 0x02, 0x00}));
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        }, "Requests slot read 1 after delay");
        assertEquals( "BB 01 00 00",
            lnis.outbound.elementAt(0).toString(),
            "Request read slot 1");
    }

    @Test
    public void testLink() {
        // slot Link 3 > 4 read 4 after delay.
        slotmanager.message(new LocoNetMessage(new int[]{0xb8, 0x03, 0x04, 0x00}));
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        }, "Requests slot read 4 after delay");
        assertEquals( "BB 04 00 00",
            lnis.outbound.elementAt(0).toString(),
            "Request read slot 4");
    }

    @Test
    public void testUnLink() {
        // slot unLink 6 from 7 read 7 after delay.
        slotmanager.message(new LocoNetMessage(new int[]{0xb9, 0x06, 0x07, 0x00}));
        JUnitUtil.waitFor(() -> {
            return !lnis.outbound.isEmpty();
        }, "Requests slot read 7 after delay");
        assertEquals( "BB 07 00 00",
            lnis.outbound.elementAt(0).toString(),
            "Request read slot 7");
    }

    @Test
    public void testClearAllNonZeroSlotsFail() {
        // test fix of error when "clear All Non-zero slots" operation is executed.
        slotmanager.message(new LocoNetMessage(new int[]{0xD4, 0x39, 0x7A, 0x60,
            0x02, 0x0A}));
        JUnitUtil.waitFor(200);
        assertEquals( 0, lnis.outbound.size(), "No Outbound Data");
        JUnitAppender.assertWarnMessage("Received slot number 250 is not in the slot map, "
            + "have you defined the wrong cammand station type? Message was D4 39 7A 60 02 0A");
    }

    private LocoNetInterfaceScaffold lnis;
    private SlotManager slotmanager;
    private int status;
    private int value;
    private boolean startedShortTimer = false;
    private boolean startedLongTimer = false;
    private boolean stoppedTimer = false;

    private ProgListener lstn;

    private int releaseTestDelay;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        jmri.InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class,
                new jmri.jmrix.ConnectionConfigManager());
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();

        slotmanager = new SlotManager(lnis) {
            @Override
            protected void startLongTimer() {
                super.startLongTimer();
                startedLongTimer = true;
            }
            @Override
            protected void startShortTimer() {
                super.startShortTimer();
                startedShortTimer = true;
            }
            @Override
            protected synchronized void stopTimer() {
                super.stopTimer();
                stoppedTimer = true;
            }
        };
        slotmanager.slotMap = Arrays.asList(new SlotMapEntry(0,127,SlotType.LOCO)); // still all slots
        slotmanager.slotScanInterval = 5;  // 5ms instead of 50
        slotmanager.pmManagerGotReply = true; // preventfurther probing
        status = -999;
        value = -999;
        startedShortTimer = false;
        startedLongTimer = false;

        releaseTestDelay = Math.max(slotmanager.serviceModeReplyDelay, slotmanager.opsModeReplyDelay)+75;

        lstn = (int val, int stat) -> {
            log.debug("   reply val: {} status: {}", val, stat);
            status = stat;
            value = val;
        };
    }

    @AfterEach
    public void tearDown() {
        slotmanager.dispose();
        lnis.dispose();
        JUnitUtil.waitThreadTerminated(slotmanager.getUserName() + SlotManager.READ_ALL_SLOTS_THREADNAME);
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlotManagerTest.class);

}
