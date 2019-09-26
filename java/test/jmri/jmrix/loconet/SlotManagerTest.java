package jmri.jmrix.loconet;

import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

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
        Assert.assertEquals("short 3 sets F9", 3,
                slotmanager.getDirectFunctionAddress(m1));

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
        Assert.assertEquals("long 513 sets F9", 513,
                slotmanager.getDirectFunctionAddress(m1));
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
        Assert.assertEquals("short 3 sets F9", 0xA1,
                slotmanager.getDirectDccPacket(m1));

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
        Assert.assertEquals("long 513 sets F9", 0xA1,
                slotmanager.getDirectDccPacket(m1));
    }

    @Test
    public void testGetSlotSend() {
        testSlot = null;
        SlotListener p2 = new SlotListener() {
            @Override
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
        };
        slotmanager.slotFromLocoAddress(0x2134, p2);
        Assert.assertEquals("slot request message",
                "BF 42 34 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("hash length", 1, slotmanager.mLocoAddrHash.size());
        Assert.assertEquals("key present", true,
                slotmanager.mLocoAddrHash.containsKey(Integer.valueOf(0x2134)));
        Assert.assertEquals("value present", true,
                slotmanager.mLocoAddrHash.contains(p2));
    }

    @Test
    public void testGetSlotRcv() {
        testSlot = null;
        SlotListener p2 = new SlotListener() {
            @Override
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
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
        Assert.assertEquals("returned slot", slotmanager.slot(11), testSlot);
        // and make sure it forgets
        testSlot = null;
        slotmanager.message(m1);
        slotmanager.message(m2);
        Assert.assertEquals("returned slot", null, testSlot);
    }

    @Test
    public void testReadCVPaged() throws jmri.ProgrammerException {
        String CV1 = "12";
        ProgListener p2 = null;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 23 00 00 00 00 00 0B 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testReadCVRegister() throws jmri.ProgrammerException {
        String CV1 = "2";
        ProgListener p2 = null;
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 13 00 00 00 00 00 01 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testReadCVDirect() throws jmri.ProgrammerException {
        log.debug(".... start testReadCVDirect ...");
        String CV1 = "29";
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.readCV(CV1, lstn);
        Assert.assertEquals("read message",
                "EF 0E 7C 2B 00 00 00 00 00 1C 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence)
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;

        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedLongTimer;},"startedLongTimer not set");
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started long timer", startedLongTimer);
        Assert.assertFalse("didn't start short timer", startedShortTimer);

        // read received back (DCS240 sequence)
        value = 0;
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B}));
        JUnitUtil.waitFor(()->{return value == 35;},"value == 35 not set");
        log.debug("checking..");
        Assert.assertEquals("reply status", 0, status);
        Assert.assertEquals("reply value", 35, value);

        log.debug(".... end testReadCVDirect ...");
    }

    @Test
    public void testReadCVOpsModeLong() throws jmri.ProgrammerException {
        String CV1 = "12";
        ProgListener p2 = null;
        slotmanager.readCVOpsMode(CV1, p2, 4 * 128 + 0x23, true);
        Assert.assertEquals("read message",
                "EF 0E 7C 2F 00 04 23 00 00 0B 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testReadCVOpsModeShort() throws jmri.ProgrammerException {
        String CV1 = "12";
        ProgListener p2 = null;
        slotmanager.readCVOpsMode(CV1, p2, 22, false);
        Assert.assertEquals("read message",
                "EF 0E 7C 2F 00 00 16 00 00 0B 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVPaged() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVRegister() throws jmri.ProgrammerException {
        String CV1 = "2";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 53 00 00 00 00 00 01 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVDirect() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 6B 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVDirectStringDCS240() throws jmri.ProgrammerException {
        log.debug(".... start testWriteCVDirectStringDCS240 ...");
        String CV1 = "31";
        int val2 = 16;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);
        Assert.assertEquals("write message",
                "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence)
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);

        // read received back (DCS240 sequence)
        value = -15;
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        JUnitUtil.waitFor(()->{return value == -1;},"value == -1 not set");
        log.debug("checking..");
        Assert.assertEquals("reply status", 0, status);
        Assert.assertEquals("reply value", -1, value);

        log.debug(".... end testWriteCVDirectStringDCS240 ...");
    }

    @Test
    public void testLackLogic() {
        LocoNetMessage m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25});
        Assert.assertTrue("checkLackTaskAccepted(m.getElement(2))", slotmanager.checkLackTaskAccepted(m.getElement(2)));
        Assert.assertFalse("checkLackProgrammerBusy(m.getElement(2))", slotmanager.checkLackProgrammerBusy(m.getElement(2)));
        Assert.assertFalse("checkLackAcceptedBlind(m.getElement(2))", slotmanager.checkLackAcceptedBlind(m.getElement(2)));
    }

    @Test
    public void testWriteCVDirectStringDCS240Interrupted() throws jmri.ProgrammerException {
        log.debug(".... start testWriteCVDirectStringDCS240 ...");
        String CV1 = "31";
        int val2 = 16;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, lstn);
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);
        Assert.assertEquals("write message",
                "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence)
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;

        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);

        // CS check received back (DCS240 sequence)
        log.debug("send CS check back");
        slotmanager.message(new LocoNetMessage(new int[]{0xBB, 0x7F, 0x00, 0x3B}));
        // not clear what to wait for here; status doesn't change
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("post-CS-check status", -999, status);

        // read received back (DCS240 sequence)
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        // not clear what to wait for here; content doesn't change
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        log.debug("checking..");
        Assert.assertEquals("reply status", 0, status);
        Assert.assertEquals("reply value", -1, value);

        log.debug(".... end testWriteCVDirectStringDCS240 ...");
    }

    @Test
    public void testWriteCVOpsLongAddr() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.writeCVOpsMode(CV1, val2, p3, 4 * 128 + 0x23, true);
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);
        Assert.assertEquals("write message",
                "EF 0E 7C 67 00 04 23 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVOpsShortAddr() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.writeCVOpsMode(CV1, val2, p3, 22, false);
        Assert.assertEquals("write message",
                "EF 0E 7C 67 00 00 16 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteThroughFacade() throws jmri.ProgrammerException {
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
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);
        Assert.assertEquals("write PI message",
                "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to PI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // completion received back (DCS240 sequence) to PI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("initial status", -999, status);

        // check that SI write happened
        Assert.assertEquals("two messages sent", 2, lnis.outbound.size());
        Assert.assertEquals("write SI message",
                "EF 0E 7C 6B 00 00 00 00 00 0F 02 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to SI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("still two messages sent", 2, lnis.outbound.size());

        // completion received back (DCS240 sequence) to SI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x0F, 0x02, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("initial status", -999, status);

        // check that final CV write happened
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("write final CV message",
                "EF 0E 7C 6B 00 00 00 00 10 00 37 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to final CV write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        JUnitUtil.waitFor(()->{return startedShortTimer;},"startedShortTimer not set");
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());

        // completion received back (DCS240 sequence)
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x10, 0x00, 0x37, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        log.debug("checking..");
        Assert.assertEquals("reply status", 0, status);
        Assert.assertEquals("reply value", -1, value);
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());

        log.debug(".... end testWriteThroughFacade ...");
    }

    @Test
    public void testReadThroughFacade() throws jmri.ProgrammerException {
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
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);
        Assert.assertEquals("write PI message",
                "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to PI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // completion received back (DCS240 sequence) to PI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1E, 0x10, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("initial status", -999, status);

        // check that SI write happened
        Assert.assertEquals("two messages sent", 2, lnis.outbound.size());
        Assert.assertEquals("write SI message",
                "EF 0E 7C 6B 00 00 00 00 00 0F 02 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to SI write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("still two messages sent", 2, lnis.outbound.size());

        // completion received back (DCS240 sequence) to SI write
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x0F, 0x02, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("initial status", -999, status);

        // check that final CV write happened
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("write final CV message",
                "EF 0E 7C 2B 00 00 00 00 10 00 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to final CV write
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25}));
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("post-LACK status", -999, status);
        Assert.assertTrue("started long timer", startedLongTimer);
        Assert.assertFalse("didn't start short timer", startedShortTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());

        // completion received back (DCS240 sequence)
        log.debug("send E7 reply back");
        slotmanager.message(new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x6B, 0x00, 0x00, 0x02, 0x47, 0x10, 0x00, 0x37, 0x7F, 0x7F, 0x4A}));
        Assert.assertEquals("no immediate reply", -999, status);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        log.debug("checking..");
        Assert.assertEquals("reply status", 0, status);
        Assert.assertEquals("reply value", 55, value);
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());

        log.debug(".... end testReadThroughFacade ...");
    }

    @Test
    public void testReadThroughFacadeFail() throws jmri.ProgrammerException {
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
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);
        Assert.assertEquals("write PI message",
                "EF 0E 7C 6B 00 00 00 00 00 1E 10 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("initial status", -999, status);

        // LACK received back (DCS240 sequence) to PI write: rejected
        log.debug("send LACK back");
        startedShortTimer = false;
        startedLongTimer = false;
        slotmanager.message(new LocoNetMessage(new int[]{0xB4, 0x6F, 0x0, 0x24}));
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);
        Assert.assertEquals("post-LACK status is fail", 4, status);
        Assert.assertFalse("didn't start short timer", startedShortTimer);
        Assert.assertFalse("didn't start long timer", startedLongTimer);
        jmri.util.JUnitUtil.releaseThread(this, releaseTestDelay);  // wait for slow reply
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());

        log.debug(".... end testReadThroughFacadeFail ...");
    }

    @Test
    public void testGetProgrammingModes() {
        List<ProgrammingMode> l = slotmanager.getSupportedModes();
        Assert.assertEquals("programming mode list length ok", 5, l.size());
        Assert.assertEquals("programming mode 0", ProgrammingMode.DIRECTBYTEMODE, l.get(0));
        Assert.assertEquals("programming mode 1", ProgrammingMode.PAGEMODE, l.get(1));
        Assert.assertEquals("programming mode 2", ProgrammingMode.REGISTERMODE, l.get(2));
        Assert.assertEquals("programming mode 3", ProgrammingMode.ADDRESSMODE, l.get(3));
        Assert.assertEquals("programming mode 4", "LOCONETCSOPSWMODE", l.get(4).getStandardName());
    }

    @Test
    public void testSendPacket() {
        byte msg[] = jmri.NmraPacket.accDecPktOpsMode(1, 4, 53);
        slotmanager.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 1",
                "ED 0B 7F 50 07 01 70 6C 03 35 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(128, 4, 53);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 2",
                "ED 0B 7F 51 07 00 50 6C 03 35 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg= jmri.NmraPacket.accDecPktOpsMode(256, 4, 53);
        slotmanager.sendPacket(msg, 3);
        Assert.assertEquals("nmra packet 3",
                "ED 0B 7F 52 07 00 30 6C 03 35 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 37, 53);
        slotmanager.sendPacket(msg, 4);
        Assert.assertEquals("nmra packet 4",
                "ED 0B 7F 53 07 01 70 6C 24 35 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 129, 53);
        slotmanager.sendPacket(msg, 5);
        Assert.assertEquals("nmra packet 5",
                "ED 0B 7F 54 0F 01 70 6C 00 35 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 10, 0);
        slotmanager.sendPacket(msg, 6);
        Assert.assertEquals("nmra packet 6",
                "ED 0B 7F 55 07 01 70 6C 09 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 10, 128);
        slotmanager.sendPacket(msg, 7);
        Assert.assertEquals("nmra packet 7",
                "ED 0B 7F 56 17 01 70 6C 09 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 10, 255);
        slotmanager.sendPacket(msg, 8);
        Assert.assertEquals("nmra packet 8",
                "ED 0B 7F 57 17 01 70 6C 09 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(511, 255, 0);
        slotmanager.sendPacket(msg, 9);
        jmri.util.JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' reduced from 9 to 8.");
        Assert.assertEquals("nmra packet 9",
                "ED 0B 7F 57 0F 3F 00 6C 7E 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(1, 31);
        slotmanager.sendPacket(msg, 0);
        jmri.util.JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' of 0 is illegal and is forced to 1.");
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 30 01 01 71 1F 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(2, 30);
        slotmanager.sendPacket(msg, -1);
        jmri.util.JUnitAppender.assertWarnMessage("Ops Mode Accessory Packet 'Send count' of -1 is illegal and is forced to 1.");
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 30 01 01 73 1E 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(4, 29);
        slotmanager.sendPacket(msg, 3);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 32 01 01 77 1D 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(8, 27);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 02 77 1B 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(16, 23);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 04 77 17 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(32, 15);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 08 77 0F 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(64, 1);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 10 77 01 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(128, 0);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 20 77 00 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(256, 2);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 00 67 02 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(512, 4);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 00 57 04 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(1024, 8);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 00 37 08 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        msg = jmri.NmraPacket.accSignalDecoderPkt(511, 16);
        slotmanager.sendPacket(msg, 2);
        Assert.assertEquals("nmra packet 10",
                "ED 0B 7F 31 01 00 55 10 00 00 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testOpcImmPacketRetry() {
        byte msg[] = jmri.NmraPacket.accDecPktOpsMode(1, 4, 54);
        slotmanager.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 1 retry test",
                "ED 0B 7F 50 07 01 70 6C 03 36 00",
               lnis.outbound.elementAt(0).toString());
        slotmanager.message(lnis.outbound.get(0));
        Assert.assertNotNull("check immedPacket non null", slotmanager.immedPacket);

        Assert.assertEquals("check that slotmanager remembers last opc_imm_packet (1)",
                new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x36, 0x00}),
                slotmanager.immedPacket);
        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6D, 0x00, 0x00}));

        JUnitUtil.waitFor(()->{return lnis.outbound.size() >1;},"retry message");
        Assert.assertEquals("retry test two messages sent", 2, lnis.outbound.size());
        Assert.assertEquals("nmra packet 2 retry test",
                "ED 0B 7F 50 07 01 70 6C 03 36 00",
                lnis.outbound.elementAt(1).toString());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 4, 55);
        slotmanager.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 2 retry test",
                "ED 0B 7F 50 07 01 70 6C 03 37 00",
               lnis.outbound.elementAt(2).toString());
        Assert.assertEquals("retry test two messages sent", 3, lnis.outbound.size());
        slotmanager.message(lnis.outbound.get(2));
        Assert.assertEquals("check that slotmanager remembers last opc_imm_packet (2)",
                new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x37, 0x00}),
                slotmanager.immedPacket);

        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6D, 0x01, 0x00}));

        Assert.assertEquals("retry test two (b) no new message sent", 3, lnis.outbound.size());

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 4, 56);
        slotmanager.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 3 retry test",
                "ED 0B 7F 50 07 01 70 6C 03 38 00",
               lnis.outbound.elementAt(3).toString());
        Assert.assertEquals("retry test three (a) messages sent", 4, lnis.outbound.size());
        slotmanager.message(lnis.outbound.get(3));
        Assert.assertEquals("check that slotmanager remembers last opc_imm_packet (3)",
                new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x38, 0x00}),
                slotmanager.immedPacket);

        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6C, 0x00, 0x00}));

        Assert.assertEquals("retry test three no new message sent", 4, lnis.outbound.size());


        msg = jmri.NmraPacket.accDecPktOpsMode(1, 4, 57);
        slotmanager.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 4 retry test",
                "ED 0B 7F 50 07 01 70 6C 03 39 00",
               lnis.outbound.elementAt(4).toString());
        Assert.assertEquals("retry test four (a) messages sent", 5, lnis.outbound.size());
        slotmanager.message(lnis.outbound.get(4));
        Assert.assertEquals("check that slotmanager remembers last opc_imm_packet (4)",
                new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x39, 0x00}),
                slotmanager.immedPacket);
        jmri.util.JUnitUtil.waitFor(500); // wait 1/2 second to ensure a retry does not happen.
        Assert.assertEquals("retry test four (b) messages sent", 5, lnis.outbound.size());

        slotmanager.message(new LocoNetMessage(new int[] {0xB3, 0x6d, 0x00, 0x00}));

        Assert.assertEquals("check mTurnoutNoRetry", false, slotmanager.mTurnoutNoRetry);
        slotmanager.setThrottledTransmitter(null, true);
        Assert.assertEquals("check mTurnoutNoRetry (2)", true, slotmanager.mTurnoutNoRetry);

        msg = jmri.NmraPacket.accDecPktOpsMode(1, 4, 33);
        slotmanager.sendPacket(msg, 1);
        Assert.assertEquals("nmra packet 5 retry test",
                "ED 0B 7F 50 07 01 70 6C 03 21 00",
               lnis.outbound.elementAt(5).toString());
        Assert.assertEquals("retry test five (a) messages sent", 6, lnis.outbound.size());
        slotmanager.message(lnis.outbound.get(5));
        Assert.assertEquals("check that slotmanager remembers last opc_imm_packet (5)",
                new LocoNetMessage(new int[]{0xED, 0x0B, 0x7F, 0x50, 0x07, 0x01, 0x70, 0x6C, 0x03, 0x21, 0x00}),
                slotmanager.immedPacket);

        slotmanager.message(new LocoNetMessage(new int[] {0xB4, 0x6C, 0x00, 0x00}));

        jmri.util.JUnitUtil.waitFor(500); // wait 1/2 second to ensure a retry does not happen.
        Assert.assertEquals("retry test five (b) messages sent", 6, lnis.outbound.size());

    }

    @Test
    public void testSlotMessageOpcPktImmFunctions() {
        testSlot = null;
        SlotListener p2 = new SlotListener() {
            @Override
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
        };
        slotmanager.slotFromLocoAddress(4121, p2);
        Assert.assertEquals("number of transmitted messages so far (a)", 1, lnis.outbound.size());
        Assert.assertEquals("slot request message",
                "BF 20 19 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("hash length", 1, slotmanager.mLocoAddrHash.size());
        Assert.assertEquals("key present", true,
                slotmanager.mLocoAddrHash.containsKey(Integer.valueOf(4121)));
        Assert.assertEquals("value present", true,
                slotmanager.mLocoAddrHash.contains(p2));
        Assert.assertNull("check testSlot still null (a)", testSlot);

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

        Assert.assertNotNull("check testSlot not null (a)", testSlot);
        Assert.assertEquals("check slot status", LnConstants.LOCO_FREE, testSlot.slotStatus());

        Assert.assertEquals("returned slot", slotmanager.slot(10), testSlot);

        Assert.assertEquals("check slot has correct address", 4121, testSlot.locoAddr());
        Assert.assertEquals("check default F9 state for slot", false, testSlot.localF9);
        Assert.assertEquals("check default F10 state for slot", false, testSlot.isF10());
        Assert.assertEquals("check default F11 state for slot", false, testSlot.isF11());
        Assert.assertEquals("check default F12 state for slot", false, testSlot.isF12());

        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        LocoNetMessage m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.message(m);

        Assert.assertEquals("check slot f9 - message wasn't accepted account slot 'free'", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());
        Assert.assertEquals("check message with correct format versus getDirectFunctionAddress",
                4121, slotmanager.getDirectFunctionAddress(m));

        // [ED 0A 7F 34 05 50 19 21 00  3E]  is not a send packet immediate message!
        m = new LocoNetMessage(new int[] {0xED, 0x0a, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x3e});
        slotmanager.message(m);

        Assert.assertEquals("check slot f9 - message wasn't accepted account wrong message length", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());
        Assert.assertEquals("check message with wrong message length versus getDirectFunctionAddress",
                -1, slotmanager.getDirectFunctionAddress(m));


        // [ED 0B 7e 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7e, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.message(m);

        Assert.assertEquals("check slot f9 - message wasn't accepted account wrong byte 2 value", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());
        Assert.assertEquals("check message with wrong byte 2 value versus getDirectFunctionAddress",
                -1, slotmanager.getDirectFunctionAddress(m));

        m.setElement(2, 0x7f);
        m.setElement(3, 0x10);
        Assert.assertEquals("check message with wrong byte 3 value(a) versus getDirectFunctionAddress",
                -1, slotmanager.getDirectFunctionAddress(m));
        m.setElement(3, 0x10);
        Assert.assertEquals("check message with wrong byte 3 value(b) versus getDirectFunctionAddress",
                -1, slotmanager.getDirectFunctionAddress(m));

        LocoNetMessage m3 = new LocoNetMessage(new int[] {0xba, 0x0a, 0x0a, 0x00});
        slotmanager.message(m3);

        m2.setElement(3,0x33);
        slotmanager.message(m2);
        Assert.assertEquals("check slot status in-use", LnConstants.LOCO_IN_USE, slotmanager.slot(10).slotStatus());
        Assert.assertEquals("check slot move message versus getDirectFunctionAddress",
                -1, slotmanager.getDirectFunctionAddress(m2));


        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.message(m);

        Assert.assertEquals("check slot f9 - message was accepted", true, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());
    }

    @Test
    public void testForwardMessageToSlotExceptions() {
        Assert.assertEquals("check 'slot in use' count is zero", 0, slotmanager.getInUseCount());
        testSlot = null;
        SlotListener p2 = new SlotListener() {
            @Override
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
        };
        slotmanager.slotFromLocoAddress(4120, p2);
        Assert.assertEquals("number of transmitted messages so far (a)", 1, lnis.outbound.size());
        Assert.assertEquals("slot request message",
                "BF 20 18 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertNull("check testSlot still null (a)", testSlot);

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

        Assert.assertNotNull("check testSlot not null (a)", testSlot);
        Assert.assertEquals("check slot status", LnConstants.LOCO_FREE, testSlot.slotStatus());

        Assert.assertEquals("returned slot", slotmanager.slot(9), testSlot);

        Assert.assertEquals("check slot has correct address", 4120, testSlot.locoAddr());
        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4120 set F9=On, F10=Off, F11=Off, F12=Off.
        LocoNetMessage m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.forwardMessageToSlot(m, 9);
        jmri.util.JUnitAppender.assertErrorMessage("slot rejected LocoNetMessage ED 0B 7F 34 05 50 19 21 00 00 3F");

        Assert.assertEquals("check slot f9 - message wasn't accepted account slot 'free'", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());

        LocoNetMessage m3 = new LocoNetMessage(new int[] {0xba, 0x09, 0x09, 0x00});
        slotmanager.message(m3);

        m2.setElement(3,0x33);
        slotmanager.message(m2);
        Assert.assertEquals("check slot status in-use", LnConstants.LOCO_IN_USE, slotmanager.slot(9).slotStatus());

        // [ED 0B 7F 34 05 50 19 21 00 00 3F]  Send packet immediate: Locomotive 4121 set F9=On, F10=Off, F11=Off, F12=Off.
        m = new LocoNetMessage(new int[] {0xED, 0x0b, 0x7f, 0x34, 0x05, 0x50, 0x19, 0x21, 0x00, 0x00, 0x3f});
        slotmanager.forwardMessageToSlot(m, 9);
        jmri.util.JUnitAppender.assertErrorMessage("slot rejected LocoNetMessage ED 0B 7F 34 05 50 19 21 00 00 3F");

        Assert.assertEquals("check slot f9 - message was not accepted (wrong address)", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());

        slotmanager.forwardMessageToSlot(m, -1);
        jmri.util.JUnitAppender.assertErrorMessage("Received slot number -1 is greater than array length 128 Message was ED 0B 7F 34 05 50 19 21 00 00 3F");

        Assert.assertEquals("check slot f9 - message was not accepted (slot number too low)", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());

        slotmanager.forwardMessageToSlot(m, 129);
        jmri.util.JUnitAppender.assertErrorMessage("Received slot number 129 is greater than array length 128 Message was ED 0B 7F 34 05 50 19 21 00 00 3F");

        Assert.assertEquals("check slot f9 - message was not accepted (slot number too high)", false, testSlot.isF9());
        Assert.assertEquals("check slot f10", false, testSlot.isF10());
        Assert.assertEquals("check slot f11", false, testSlot.isF11());
        Assert.assertEquals("check slot f12", false, testSlot.isF12());

        Assert.assertEquals("check 'slot in use' count is one", 1, slotmanager.getInUseCount());
    }

    @Test
    public void testGetWriteConfirmMode() {
        Assert.assertEquals("check geWriteConfirmMode('abcd')",
                jmri.Programmer.WriteConfirmMode.DecoderReply,
                slotmanager.getWriteConfirmMode("abcd"));
    }

    @Test
    public void testGetUserName() {
        Assert.assertEquals("check getUserName","LocoNet", slotmanager.getUserName());
    }

    @Test
    public void testOpCode8a() {

        LocoNetMessage m = new LocoNetMessage(new int[] {0x8a, 0x75});
        
        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS100;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        Assert.assertEquals("check no messages sent when DCS100", 0, lnis.outbound.size());
        
        
        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS051;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        Assert.assertEquals("check no messages sent when DCS051", 0, lnis.outbound.size());
        
        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS050;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        Assert.assertEquals("check no messages sent when DCS050", 0, lnis.outbound.size());
        
        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DB150;
        slotmanager.message(m);
        JUnitUtil.waitFor(600);
        Assert.assertEquals("check no messages sent when DB150", 0, lnis.outbound.size());

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS240;
        slotmanager.message(new LocoNetMessage(new int[] {0x8a, 0x75}));
        JUnitUtil.waitFor(()->{return lnis.outbound.size() >126;},"testOpCode8a: slot managersent at least 127 LocoNet messages");
        for (int i = 0; i < 127; ++i) {
            Assert.assertEquals("testOpCode8a: loop "+i+" check sent opcode", 0xBB, lnis.outbound.get(i).getOpCode());
            Assert.assertEquals("testOpCode8a: loop "+i+" check sent byte 1", i, lnis.outbound.get(i).getElement(1));
            Assert.assertEquals("testOpCode8a: loop "+i+" check sent byte 2", 0, lnis.outbound.get(i).getElement(2));

        }
    }

    @Test
    public void testMoreOpCode8a() {

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS210;
        slotmanager.message(new LocoNetMessage(new int[] {0x8a, 0x75}));
        JUnitUtil.waitFor(()->{return lnis.outbound.size() >126;},"testOpCode8a: slot managersent at least 127 LocoNet messages");
        for (int i = 0; i < 127; ++i) {
            Assert.assertEquals("testOpCode8a DCS210: loop "+i+" check sent opcode", 0xBB, lnis.outbound.get(i).getOpCode());
            Assert.assertEquals("testOpCode8a DCS210: loop "+i+" check sent byte 1", i, lnis.outbound.get(i).getElement(1));
            Assert.assertEquals("testOpCode8a DCS210: loop "+i+" check sent byte 2", 0, lnis.outbound.get(i).getElement(2));

        }
    }

    @Test
    public void testEvenMoreOpCode8a() {

        slotmanager.commandStationType = LnCommandStationType.COMMAND_STATION_DCS052;
        slotmanager.message(new LocoNetMessage(new int[] {0x8a, 0x75}));
        JUnitUtil.waitFor(()->{return lnis.outbound.size() >126;},"testOpCode8a: slot managersent at least 127 LocoNet messages");
        for (int i = 0; i < 127; ++i) {
            Assert.assertEquals("testOpCode8a DCS052: loop "+i+" check sent opcode", 0xBB, lnis.outbound.get(i).getOpCode());
            Assert.assertEquals("testOpCode8a DCS052: loop "+i+" check sent byte 1", i, lnis.outbound.get(i).getElement(1));
            Assert.assertEquals("testOpCode8a DCS052: loop "+i+" check sent byte 2", 0, lnis.outbound.get(i).getElement(2));

        }
    }

    // The minimal setup for log4J
    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    int status;
    int value;
    boolean startedShortTimer = false;
    boolean startedLongTimer = false;
    boolean stoppedTimer = false;

    ProgListener lstn;
    int releaseTestDelay = 150; // probably needs to be at least 150, see SlotManager.postProgDelay

    @Before
    public void setUp() {
        JUnitUtil.setUp();

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
            protected void stopTimer() {
                super.stopTimer();
                stoppedTimer = true;
            }
        };

        status = -999;
        value = -999;
        startedShortTimer = false;
        startedLongTimer = false;

        lstn = new ProgListener(){
            @Override
            public void programmingOpReply(int val, int stat) {
                log.debug("   reply val: {} status: {}", val, stat);
                status = stat;
                value = val;
            }
        };
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlotManager.class);

}
