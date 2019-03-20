package jmri.jmrix.loconet;

import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Large parts are copied from SlotManagerTest; combining these would be good.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class LnDeferProgrammerTest {

    @Test
    public void testCTor() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        LnDeferProgrammer t = new LnDeferProgrammer(memo);
        Assert.assertNotNull("exists", t);
        memo.dispose();
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


    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    int status;
    int value;
    boolean startedShortTimer = false;
    boolean startedLongTimer = false;
    boolean stoppedTimer = false;

    ProgListener lstn;
    int releaseTestDelay = 150; // probably needs to be at least 150, see SlotManager.postProgDelay

    // The minimal setup for log4J
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnDeferProgrammerTest.class);

}
