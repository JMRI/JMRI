package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Large parts are copied from SlotManagerTest; combining these would be good.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class LnDeferProgrammerTest {

    @Test
    public void testCTor() {

        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        LnDeferProgrammer t = new LnDeferProgrammer(memo);
        assertNotNull( t, "exists");
        memo.dispose();
    }


    @Test
    public void testReadCVPaged() throws jmri.ProgrammerException {
        String CV1 = "12";
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.readCV(CV1, null);
        assertEquals( "EF 0E 7C 23 00 00 00 00 00 0B 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testReadCVRegister() throws jmri.ProgrammerException {
        String CV1 = "2";
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.readCV(CV1, null);
        assertEquals( "EF 0E 7C 13 00 00 00 00 00 01 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testReadCVDirect() throws jmri.ProgrammerException {
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

        log.debug(".... end testReadCVDirect ...");
    }

    @Test
    public void testReadCVOpsModeLong() throws jmri.ProgrammerException {
        String CV1 = "12";
        slotmanager.readCVOpsMode(CV1, null, 4 * 128 + 0x23, true);
        assertEquals( "EF 0E 7C 2F 00 04 23 00 00 0B 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testReadCVOpsModeShort() throws jmri.ProgrammerException {
        String CV1 = "12";
        slotmanager.readCVOpsMode(CV1, null, 22, false);
        assertEquals( "EF 0E 7C 2F 00 00 16 00 00 0B 00 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "read message");
    }

    @Test
    public void testWriteCVPaged() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.writeCV(CV1, val2, null);
        assertEquals( "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVPagedString() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.PAGEMODE);
        slotmanager.writeCV(CV1, val2, null);
        assertEquals( "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVRegister() throws jmri.ProgrammerException {
        String CV1 = "2";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.REGISTERMODE);
        slotmanager.writeCV(CV1, val2, null);
        assertEquals( "EF 0E 7C 53 00 00 00 00 00 01 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVDirect() throws jmri.ProgrammerException {
        String CV1 = "12";
        int val2 = 34;
        slotmanager.setMode(ProgrammingMode.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, null);
        assertEquals( "EF 0E 7C 6B 00 00 00 00 00 0B 22 7F 7F 00",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "write message");
    }

    @Test
    public void testWriteCVDirectStringDCS240() throws jmri.ProgrammerException {
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
        assertTrue(stoppedTimer);

        log.debug(".... end testWriteCVDirectStringDCS240 ...");
    }


    private LocoNetInterfaceScaffold lnis;
    private SlotManager slotmanager;
    private int status;
    private int value;
    private boolean startedShortTimer = false;
    private boolean startedLongTimer = false;
    private boolean stoppedTimer = false;

    private ProgListener lstn;
    // private final int releaseTestDelay = 150; // probably needs to be at least 150, see SlotManager.postProgDelay

    @BeforeEach
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
            protected synchronized void stopTimer() {
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

    @AfterEach
    public void tearDown() {
        lnis.dispose();
        slotmanager.dispose();
        lnis = null;
        slotmanager = null;
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnDeferProgrammerTest.class);

}
