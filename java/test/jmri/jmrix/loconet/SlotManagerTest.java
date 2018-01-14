package jmri.jmrix.loconet;

import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

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
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 23 00 00 00 00 00 0B 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    // Test names ending with "String" are for the new writeCV(String, ...)
    // etc methods.  If you remove the older writeCV(int, ...) tests,
    // you can rename these. Note that not all (int,...) tests may have a
    // String(String, ...) test defined, in which case you should create those.
    @Test
    public void testReadCVPagedString() throws jmri.ProgrammerException {
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
        int CV1 = 2;
        ProgListener p2 = null;
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 13 00 00 00 00 00 01 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testReadCVRegisterString() throws jmri.ProgrammerException {
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
        int CV1 = 29;
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
    public void testReadCVDirectString() throws jmri.ProgrammerException {
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
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.readCVOpsMode(CV1, p2, 4 * 128 + 0x23, true);
        Assert.assertEquals("read message",
                "EF 0E 7C 2F 00 04 23 00 00 0B 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testReadCVOpsModeShort() throws jmri.ProgrammerException {
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.readCVOpsMode(CV1, p2, 22, false);
        Assert.assertEquals("read message",
                "EF 0E 7C 2F 00 00 16 00 00 0B 00 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVPaged() throws jmri.ProgrammerException {
        int CV1 = 12;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVPagedString() throws jmri.ProgrammerException {
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
        int CV1 = 2;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 53 00 00 00 00 00 01 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVRegisterString() throws jmri.ProgrammerException {
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
        int CV1 = 12;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 6B 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testWriteCVDirectString() throws jmri.ProgrammerException {
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
        int CV1 = 12;
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
        int CV1 = 12;
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
        Assert.assertEquals("programming mode 0", ProgrammingMode.PAGEMODE, l.get(0));
        Assert.assertEquals("programming mode 1", ProgrammingMode.DIRECTBYTEMODE, l.get(1));
        Assert.assertEquals("programming mode 2", ProgrammingMode.REGISTERMODE, l.get(2));
        Assert.assertEquals("programming mode 3", ProgrammingMode.ADDRESSMODE, l.get(3));
        Assert.assertEquals("programming mode 4", "LOCONETCSOPSWMODE", l.get(4).getStandardName());
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
